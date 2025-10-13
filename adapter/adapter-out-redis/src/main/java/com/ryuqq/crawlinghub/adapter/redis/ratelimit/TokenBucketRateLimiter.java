package com.ryuqq.crawlinghub.adapter.redis.ratelimit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Token Bucket Rate Limiter
 * Redis Lua Script 기반 분산 레이트 리미팅
 *
 * 성능 목표:
 * - p99 latency < 10ms
 * - Throughput > 10,000 ops/sec
  *
 * @author crawlinghub
 */
@Service
public class TokenBucketRateLimiter {

    private static final String BUCKET_KEY_PREFIX = "rate_limit:bucket:";
    private static final int DEFAULT_TTL_SECONDS = 3600; // 1시간

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<List> tokenBucketScript;

    public TokenBucketRateLimiter(
            RedisTemplate<String, Object> redisTemplate,
            RedisScript<List> tokenBucketScript) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
    }

    /**
     * Token 소비 시도
     *
     * @param userAgentId User-Agent ID
     * @param tokensToConsume 소비할 토큰 수
     * @param refillRate 재충전 속도 (tokens per second)
     * @param maxTokens 최대 토큰 수
     * @return RateLimitResult
      *
 * @author crawlinghub
 */
    public RateLimitResult tryConsume(
            Long userAgentId,
            int tokensToConsume,
            double refillRate,
            int maxTokens) {

        String bucketKey = BUCKET_KEY_PREFIX + userAgentId;
        long currentTimestamp = System.currentTimeMillis();

        List<String> keys = Collections.singletonList(bucketKey);
        Object[] args = new Object[] {
            tokensToConsume,
            currentTimestamp,
            refillRate,
            maxTokens,
            DEFAULT_TTL_SECONDS
        };

        @SuppressWarnings("unchecked")
        List<Number> result = (List<Number>) redisTemplate.execute(
            tokenBucketScript,
            keys,
            args
        );

        if (result == null || result.size() < 3) {
            throw new IllegalStateException("Invalid response from token bucket script");
        }

        long success = result.get(0).longValue();
        double currentTokens = result.get(1).doubleValue();
        long retryAfterMs = result.get(2).longValue();

        return new RateLimitResult(
            success == 1,
            currentTokens,
            retryAfterMs
        );
    }

    /**
     * 기본 설정으로 Token 소비 시도
     * - 10분당 80 requests (머스트잇 API 제약)
     * - 1 token per request
      *
 * @author crawlinghub
 */
    public RateLimitResult tryConsumeDefault(Long userAgentId) {
        int maxTokens = 80;
        double refillRate = 80.0 / 600.0; // 0.1333 tokens/sec
        int tokensToConsume = 1;

        return tryConsume(userAgentId, tokensToConsume, refillRate, maxTokens);
    }

    /**
     * 현재 Bucket 상태 조회
      *
 * @author crawlinghub
 */
    public BucketStatus getBucketStatus(Long userAgentId) {
        String bucketKey = BUCKET_KEY_PREFIX + userAgentId;

        Object tokens = redisTemplate.opsForHash().get(bucketKey, "tokens");
        Object lastRefill = redisTemplate.opsForHash().get(bucketKey, "last_refill_timestamp");
        Object maxTokens = redisTemplate.opsForHash().get(bucketKey, "max_tokens");
        Object refillRate = redisTemplate.opsForHash().get(bucketKey, "refill_rate");

        if (tokens == null) {
            return null;
        }

        return new BucketStatus(
            Double.parseDouble(tokens.toString()),
            Long.parseLong(lastRefill.toString()),
            Integer.parseInt(maxTokens.toString()),
            Double.parseDouble(refillRate.toString())
        );
    }

    /**
     * Rate Limit Result
      *
 * @author crawlinghub
 */
    public static class RateLimitResult {
        private final boolean allowed;
        private final double currentTokens;
        private final long retryAfterMs;

        public RateLimitResult(boolean allowed, double currentTokens, long retryAfterMs) {
            this.allowed = allowed;
            this.currentTokens = currentTokens;
            this.retryAfterMs = retryAfterMs;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public double getCurrentTokens() {
            return currentTokens;
        }

        public long getRetryAfterMs() {
            return retryAfterMs;
        }

        @Override
        public String toString() {
            return "RateLimitResult{" +
                    "allowed=" + allowed +
                    ", currentTokens=" + currentTokens +
                    ", retryAfterMs=" + retryAfterMs +
                    '}';
        }
    }

    /**
     * Bucket Status
      *
 * @author crawlinghub
 */
    public static class BucketStatus {
        private final double currentTokens;
        private final long lastRefillTimestamp;
        private final int maxTokens;
        private final double refillRate;

        public BucketStatus(
                double currentTokens,
                long lastRefillTimestamp,
                int maxTokens,
                double refillRate) {
            this.currentTokens = currentTokens;
            this.lastRefillTimestamp = lastRefillTimestamp;
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
        }

        public double getCurrentTokens() {
            return currentTokens;
        }

        public long getLastRefillTimestamp() {
            return lastRefillTimestamp;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public double getRefillRate() {
            return refillRate;
        }
    }
}
