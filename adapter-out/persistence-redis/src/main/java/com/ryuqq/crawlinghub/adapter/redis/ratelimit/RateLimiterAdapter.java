package com.ryuqq.crawlinghub.adapter.redis.ratelimit;

import com.ryuqq.crawlinghub.application.token.port.RateLimiterPort;
import org.springframework.stereotype.Component;

/**
 * Rate Limiter Adapter
 * <p>
 * Redis 기반 Token Bucket Rate Limiting
 * - 시간당 80회 요청 제한
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class RateLimiterAdapter implements RateLimiterPort {

    private final TokenBucketRateLimiter rateLimiter;

    public RateLimiterAdapter(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * 요청 소비 시도
     *
     * @param userAgentId User-Agent ID
     * @return 소비 성공 여부
     */
    @Override
    public boolean tryConsume(Long userAgentId) {
        TokenBucketRateLimiter.RateLimitResult result = rateLimiter.tryConsumeDefault(userAgentId);
        return result.isAllowed();
    }

    /**
     * 대기 시간 조회
     * <p>
     * Token이 재충전될 때까지의 대기 시간
     * </p>
     *
     * @param userAgentId User-Agent ID
     * @param tokensRequired 필요한 토큰 수
     * @return 대기 시간 (밀리초)
     */
    @Override
    public long getWaitTime(Long userAgentId, int tokensRequired) {
        // Token 재충전 시간 계산 (80 requests / 10 minutes = 1 request per 7.5초)
        long refillIntervalMs = (10 * 60 * 1000) / 80;  // 7500ms
        return refillIntervalMs * tokensRequired;
    }
}
