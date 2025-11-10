package com.ryuqq.crawlinghub.application.token.manager;

import com.ryuqq.crawlinghub.application.token.port.*;
import com.ryuqq.crawlinghub.application.token.service.TokenTransactionService;
import com.ryuqq.crawlinghub.domain.token.Token;
import com.ryuqq.crawlinghub.domain.token.exception.TokenAcquisitionException;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import org.springframework.stereotype.Component;

/**
 * 토큰 획득을 담당하는 Manager
 * <p>
 * ⭐ Domain 중심 설계:
 * - UserAgent Domain 객체 사용
 * - Token VO 사용
 * - 비즈니스 로직은 Domain에 위임
 * </p>
 * <p>
 * ⚠️ Transaction 경계:
 * - Manager 자체에는 @Transactional 없음
 * - TokenTransactionService에 위임하여 짧은 트랜잭션 실행
 * - 외부 API 호출은 트랜잭션 밖에서 수행
 * </p>
 * <p>
 * 역할:
 * - User-Agent Pool에서 LRU 방식으로 에이전트 선택
 * - 분산 락을 통한 동시성 제어
 * - 토큰 만료 시 외부 API 호출로 신규 토큰 발급
 * - Rate Limiter를 통한 요청 제한 확인
 * - 토큰 사용 기록
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class TokenAcquisitionManager {

    private static final String LOCK_KEY_PREFIX = "token:lock:";
    private static final long LOCK_TIMEOUT_MS = 5000L;

    private final UserAgentPoolPort poolPort;
    private final DistributedLockPort lockPort;
    private final RateLimiterPort rateLimiterPort;
    private final CircuitBreakerPort circuitBreakerPort;
    private final MustItTokenPort mustItTokenPort;
    private final TokenTransactionService transactionService;

    public TokenAcquisitionManager(
            UserAgentPoolPort poolPort,
            DistributedLockPort lockPort,
            RateLimiterPort rateLimiterPort,
            CircuitBreakerPort circuitBreakerPort,
            MustItTokenPort mustItTokenPort,
            TokenTransactionService transactionService
    ) {
        this.poolPort = poolPort;
        this.lockPort = lockPort;
        this.rateLimiterPort = rateLimiterPort;
        this.circuitBreakerPort = circuitBreakerPort;
        this.mustItTokenPort = mustItTokenPort;
        this.transactionService = transactionService;
    }

    /**
     * 토큰 획득 (Domain 중심 Flow)
     * <p>
     * 7-Step Flow:
     * 1. Redis Pool에서 LRU 방식으로 User-Agent 선택
     * 2. 분산 락 획득 (동시성 제어)
     * 3. [TX1] DB에서 User-Agent 정보 조회 (readOnly)
     * 4. 토큰 만료 여부 확인 및 신규 발급
     * 5. Rate Limiter 확인
     * 6. [TX3] 토큰 사용 기록
     * 7. UserAgent Domain 객체 반환
     * </p>
     *
     * @return UserAgent Domain 객체 (토큰 포함)
     * @throws TokenAcquisitionException 토큰 획득 실패 시
     */
    public UserAgent acquireToken() {
        // Step 1: Redis Pool에서 LRU 방식으로 User-Agent 선택
        UserAgent userAgent = selectUserAgentFromPool();

        // Step 2: 분산 락 획득 (동시성 제어)
        String lockKey = buildLockKey(userAgent.getIdValue());
        String lockValue = acquireDistributedLock(lockKey);

        try {
            // Step 3: [TX1] DB에서 최신 User-Agent 정보 로드
            UserAgent loadedUserAgent = transactionService.loadUserAgent(userAgent.getIdValue());

            // Step 4: 토큰 유효성 확인 및 발급 (Domain 로직 활용)
            if (loadedUserAgent.isTokenExpired()) {
                Token newToken = issueNewTokenFromExternalApi(loadedUserAgent.getUserAgentString());

                // ⭐ Domain 메서드로 토큰 발급 (비즈니스 규칙 적용)
                loadedUserAgent.issueNewToken(newToken);

                // [TX2] DB 저장
                transactionService.saveUserAgent(loadedUserAgent);
            }

            // Step 5: Rate Limiter 확인
            validateRateLimit(loadedUserAgent.getIdValue());

            // Step 6: ⭐ Domain 메서드로 요청 소비 (비즈니스 규칙 적용)
            loadedUserAgent.consumeRequest();

            // [TX3] 사용 기록 저장
            transactionService.recordUsage(loadedUserAgent);

            // Step 7: Domain 객체 반환
            return loadedUserAgent;

        } finally {
            // 락 해제
            releaseDistributedLock(lockKey, lockValue);
        }
    }

    /**
     * Step 1: Pool에서 User-Agent 선택
     */
    private UserAgent selectUserAgentFromPool() {
        UserAgent userAgent = poolPort.acquireLeastRecentlyUsed();

        if (userAgent == null) {
            throw new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.NO_AVAILABLE_USER_AGENT,
                "사용 가능한 User-Agent가 없습니다"
            );
        }

        return userAgent;
    }

    /**
     * Step 2: 분산 락 획득
     */
    private String acquireDistributedLock(String lockKey) {
        String lockValue = lockPort.tryAcquire(lockKey, LOCK_TIMEOUT_MS);

        if (lockValue == null) {
            throw new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.LOCK_ACQUISITION_FAILED,
                "분산 락 획득에 실패했습니다: " + lockKey
            );
        }

        return lockValue;
    }

    /**
     * 외부 API로 신규 토큰 발급 (Domain Token VO 반환)
     * ⚠️ 트랜잭션 밖에서 호출
     */
    private Token issueNewTokenFromExternalApi(String userAgentString) {
        // Circuit Breaker 확인
        if (circuitBreakerPort.isOpen(userAgentString)) {
            throw new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.CIRCUIT_BREAKER_OPEN,
                "Circuit Breaker가 열려있어 토큰 발급이 불가능합니다"
            );
        }

        try {
            // ⭐ Domain Token VO 반환
            Token token = mustItTokenPort.issueToken(userAgentString);

            // Circuit Breaker 성공 기록
            circuitBreakerPort.recordSuccess(userAgentString);

            return token;

        } catch (Exception e) {
            // Circuit Breaker 실패 기록
            circuitBreakerPort.recordFailure(userAgentString);

            throw new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.TOKEN_ISSUANCE_FAILED,
                "외부 API에서 토큰 발급에 실패했습니다",
                e
            );
        }
    }

    /**
     * Step 5: Rate Limiter 확인
     */
    private void validateRateLimit(Long userAgentId) {
        boolean allowed = rateLimiterPort.tryConsume(userAgentId);

        if (!allowed) {
            // Pool로 반환 (실패 시에도 다른 요청이 사용할 수 있도록)
            // poolPort.returnToPool(userAgent); // TODO: UserAgent 객체 전달 필요

            long waitTimeMs = rateLimiterPort.getWaitTime(userAgentId, 1);

            throw new TokenAcquisitionException(
                TokenAcquisitionException.ErrorCode.RATE_LIMIT_EXCEEDED,
                String.format("Rate Limit 초과. %dms 후 재시도 가능", waitTimeMs)
            );
        }
    }

    /**
     * 분산 락 해제
     */
    private void releaseDistributedLock(String lockKey, String lockValue) {
        try {
            lockPort.release(lockKey, lockValue);
        } catch (Exception e) {
            // 락 해제 실패는 로깅만 (타임아웃으로 자동 해제됨)
            // TODO: Logger 추가
        }
    }

    /**
     * Lock Key 생성
     */
    private String buildLockKey(Long userAgentId) {
        return LOCK_KEY_PREFIX + userAgentId;
    }
}
