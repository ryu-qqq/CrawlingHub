package com.ryuqq.crawlinghub.adapter.redis.ratelimit;

import com.ryuqq.crawlinghub.application.token.port.RateLimiterPort;
import org.springframework.stereotype.Component;

/**
 * Rate Limiter Adapter
 *
 * @author crawlinghub
 */
@Component
public class RateLimiterAdapter implements RateLimiterPort {

    private final TokenBucketRateLimiter rateLimiter;

    public RateLimiterAdapter(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean tryConsume(Long userAgentId) {
        TokenBucketRateLimiter.RateLimitResult result = rateLimiter.tryConsumeDefault(userAgentId);
        return result.isAllowed();
    }

    @Override
    public void initialize(Long userAgentId) {
        // Token Bucket 초기화 (첫 호출 시 자동 생성)
        rateLimiter.tryConsumeDefault(userAgentId);
    }
}
