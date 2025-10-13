package com.ryuqq.crawlinghub.application.token.usecase;

import com.ryuqq.crawlinghub.application.token.port.*;
import com.ryuqq.crawlinghub.domain.token.AcquiredToken;
import com.ryuqq.crawlinghub.domain.token.TokenAcquisitionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Acquire Token Use Case
 * 토큰 획득 전체 플로우 orchestration
 *
 * @author crawlinghub
 */
@Service
public class AcquireTokenUseCase {

    private final UserAgentPoolPort poolPort;
    private final UserAgentTokenPort tokenPort;
    private final DistributedLockPort lockPort;
    private final CircuitBreakerPort circuitBreakerPort;
    private final RateLimiterPort rateLimiterPort;

    public AcquireTokenUseCase(
            UserAgentPoolPort poolPort,
            UserAgentTokenPort tokenPort,
            DistributedLockPort lockPort,
            CircuitBreakerPort circuitBreakerPort,
            RateLimiterPort rateLimiterPort) {
        this.poolPort = poolPort;
        this.tokenPort = tokenPort;
        this.lockPort = lockPort;
        this.circuitBreakerPort = circuitBreakerPort;
        this.rateLimiterPort = rateLimiterPort;
    }

    /**
     * 토큰 획득
     *
     * 플로우:
     * 1. Redis Pool에서 LRU User-Agent 선택
     * 2. 분산 락 획득
     * 3. Circuit Breaker 확인
     * 4. DB에서 User-Agent 및 Token 조회
     * 5. Rate Limiter 확인
     * 6. DB 사용 통계 업데이트
     * 7. AcquiredToken 반환
     *
     * @return AcquiredToken
     * @throws TokenAcquisitionException 토큰 획득 실패 시
     */
    @Transactional
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
            // 3. Circuit Breaker 확인
            if (circuitBreakerPort.isOpen(userAgentId)) {
                throw new TokenAcquisitionException(TokenAcquisitionException.Reason.CIRCUIT_BREAKER_OPEN);
            }

            // 4. DB에서 User-Agent 및 Token 조회
            UserAgentInfo userAgentInfo = tokenPort.findActiveToken(userAgentId);
            if (userAgentInfo == null) {
                throw new TokenAcquisitionException(TokenAcquisitionException.Reason.INVALID_USER_AGENT);
            }

            if (userAgentInfo.isExpired()) {
                throw new TokenAcquisitionException(TokenAcquisitionException.Reason.TOKEN_EXPIRED);
            }

            // 5. Rate Limiter 확인
            if (!rateLimiterPort.tryConsume(userAgentId)) {
                throw new TokenAcquisitionException(TokenAcquisitionException.Reason.RATE_LIMIT_EXCEEDED);
            }

            // 6. DB 사용 통계 업데이트
            tokenPort.recordUsage(userAgentId);

            // 7. AcquiredToken 반환
            return new AcquiredToken(
                    userAgentId,
                    userAgentInfo.userAgent(),
                    userAgentInfo.tokenValue(),
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
