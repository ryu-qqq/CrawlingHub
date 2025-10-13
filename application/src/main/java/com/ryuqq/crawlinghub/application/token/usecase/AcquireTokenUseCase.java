package com.ryuqq.crawlinghub.application.token.usecase;

import com.ryuqq.crawlinghub.application.token.port.*;
import com.ryuqq.crawlinghub.application.token.service.TokenTransactionService;
import com.ryuqq.crawlinghub.domain.token.AcquiredToken;
import com.ryuqq.crawlinghub.domain.token.TokenAcquisitionException;
import com.ryuqq.crawlinghub.domain.token.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Acquire Token Use Case
 * 
 * 토큰 획득 플로우 오케스트레이션 (트랜잭션 관리 제외)
 * 
 * 설계 원칙:
 * - UseCase는 오케스트레이션만 담당 (트랜잭션 경계 없음)
 * - 트랜잭션 관리는 TokenTransactionService에 위임
 * - 외부 API 호출은 트랜잭션 밖에서 실행 (커넥션 점유 방지)
 * - 각 DB 작업은 짧고 독립적인 트랜잭션으로 실행
 * 
 * Spring AOP 프록시 한계 해결:
 * - 내부 메서드 호출 시 @Transactional이 작동하지 않음
 * - 별도 Service로 분리하여 프록시를 통한 호출 보장
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 * @since 2025-10-14
 */
@Service
public class AcquireTokenUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(AcquireTokenUseCase.class);

    private final UserAgentPoolPort poolPort;
    private final DistributedLockPort lockPort;
    private final RateLimiterPort rateLimiterPort;
    private final CircuitBreakerPort circuitBreakerPort;
    private final MustitTokenPort mustitTokenPort;
    private final TokenTransactionService transactionService;

    public AcquireTokenUseCase(
            UserAgentPoolPort poolPort,
            DistributedLockPort lockPort,
            RateLimiterPort rateLimiterPort,
            CircuitBreakerPort circuitBreakerPort,
            MustitTokenPort mustitTokenPort,
            TokenTransactionService transactionService) {
        this.poolPort = poolPort;
        this.lockPort = lockPort;
        this.rateLimiterPort = rateLimiterPort;
        this.circuitBreakerPort = circuitBreakerPort;
        this.mustitTokenPort = mustitTokenPort;
        this.transactionService = transactionService;
    }

    /**
     * 토큰 획득 (오케스트레이션)
     * 
     * ⚠️ 트랜잭션 없음 - 각 단계별로 독립된 짧은 트랜잭션 실행
     *
     * 플로우:
     * 1. Redis Pool에서 LRU User-Agent 선택 (Redis, 비트랜잭션)
     * 2. 분산 락 획득 (Redis, 비트랜잭션)
     * 3. [트랜잭션 1] DB에서 User-Agent 및 Token 조회 (readOnly, 짧음)
     * 4. 토큰 만료 시:
     *    - 외부 API 호출 (Mustit, 3-10초, 비트랜잭션)
     *    - [트랜잭션 2] DB에 새 토큰 저장 (write, 짧음)
     * 5. Rate Limiter 확인 (Redis, 비트랜잭션)
     * 6. [트랜잭션 3] 사용 통계 업데이트 (write, 짧음)
     * 7. AcquiredToken 반환
     *
     * @return AcquiredToken
     * @throws TokenAcquisitionException 토큰 획득 실패 시
     */
    public AcquiredToken execute() {
        // 1. Redis Pool에서 LRU User-Agent 선택
        Long userAgentId = poolPort.acquireLeastRecentlyUsed();
        if (userAgentId == null) {
            throw new TokenAcquisitionException(TokenAcquisitionException.Reason.POOL_EXHAUSTED);
        }

        // 2. 분산 락 획득
        String lockKey = "user_agent:lock:" + userAgentId;
        String lockValue = lockPort.tryAcquire(lockKey);
        if (lockValue == null) {
            poolPort.returnToPool(userAgentId);
            throw new TokenAcquisitionException(TokenAcquisitionException.Reason.LOCK_ACQUISITION_FAILED);
        }

        try {
            // 3. [트랜잭션 1] DB에서 User-Agent 및 Token 조회 (readOnly)
            //    - Circuit Breaker 확인 포함
            //    - 짧은 트랜잭션 (DB 조회만)
            UserAgentInfo userAgentInfo = transactionService.loadUserAgentWithToken(userAgentId);

            String tokenValue = userAgentInfo.tokenValue();

            // 4. 토큰 만료 시 재발급
            if (userAgentInfo.tokenValue() == null || userAgentInfo.isExpired()) {
                LOG.info("Token expired or missing for userAgentId: {}. Issuing new token from Mustit...", userAgentId);

                try {
                    // 외부 API 호출 (트랜잭션 밖, 3-10초 소요 가능)
                    // ⚠️ DB 커넥션 점유하지 않음
                    TokenResponse newToken = mustitTokenPort.issueToken(userAgentInfo.userAgent());

                    // [트랜잭션 2] DB에 새 토큰 저장 (write)
                    //    - 짧은 트랜잭션 (DB 쓰기만)
                    transactionService.saveNewToken(userAgentId, newToken);

                    tokenValue = newToken.accessToken();

                    LOG.info("New token issued successfully for userAgentId: {}", userAgentId);

                } catch (Exception e) {
                    LOG.error("Failed to issue new token from Mustit for userAgentId: {}", userAgentId, e);

                    // 실패 통계 기록 (독립 트랜잭션)
                    transactionService.recordTokenFailure(userAgentId);
                    circuitBreakerPort.recordFailure(userAgentId);

                    throw new TokenAcquisitionException(
                            TokenAcquisitionException.Reason.TOKEN_EXPIRED,
                            e
                    );
                }
            }

            // 5. Rate Limiter 확인
            if (!rateLimiterPort.tryConsume(userAgentId)) {
                throw new TokenAcquisitionException(TokenAcquisitionException.Reason.RATE_LIMIT_EXCEEDED);
            }

            // 6. [트랜잭션 3] 사용 통계 업데이트 (write)
            //    - 짧은 트랜잭션 (DB 쓰기만)
            //    - 다른 트랜잭션 실패와 독립적
            transactionService.recordTokenUsage(userAgentId);

            // 7. AcquiredToken 반환
            return new AcquiredToken(
                    userAgentId,
                    userAgentInfo.userAgent(),
                    tokenValue,
                    lockKey,
                    lockValue
            );

        } catch (TokenAcquisitionException e) {
            lockPort.release(lockKey, lockValue);
            poolPort.returnToPool(userAgentId);
            throw e;
        }
    }
}
