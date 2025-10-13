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
     * 다음 Token 사용 가능 시각까지 대기 시간 계산
     *
     * @param userAgentId User-Agent ID
     * @return 대기 시간 (밀리초), 즉시 사용 가능하면 0
     * @author crawlinghub
     */
    public long getWaitTime(Long userAgentId) {
        return getWaitTime(userAgentId, 1);
    }

    /**
     * 특정 토큰 수 사용을 위한 대기 시간 계산
     *
     * @param userAgentId User-Agent ID
     * @param tokensRequired 필요한 토큰 수
     * @return 대기 시간 (밀리초), 최대 10분(600,000ms)
     * @author crawlinghub
     */
    public long getWaitTime(Long userAgentId, int tokensRequired) {
        BucketStatus status = getBucketStatus(userAgentId);

        if (status == null) {
            // Bucket이 없으면 즉시 사용 가능
            return 0L;
        }

        long now = System.currentTimeMillis();
        long elapsedMs = now - status.getLastRefillTimestamp();
        double elapsedSeconds = elapsedMs / 1000.0;

        // Refill 계산
        double tokensAfterRefill = Math.min(
            status.getMaxTokens(),
            status.getCurrentTokens() + (elapsedSeconds * status.getRefillRate())
        );

        if (tokensAfterRefill >= tokensRequired) {
            // 충분한 토큰이 있음
            return 0L;
        }

        // 부족한 토큰 수 계산
        double shortage = tokensRequired - tokensAfterRefill;
        double waitSeconds = shortage / status.getRefillRate();
        long waitMs = (long) Math.ceil(waitSeconds * 1000);

        // 최대 대기 시간 10분 제한
        long MAX_WAIT_MS = 600_000L;
        return Math.min(waitMs, MAX_WAIT_MS);
    }

    /**
     * Bucket 설정 동적 조정
     *
     * @param userAgentId User-Agent ID
     * @param maxTokens 새로운 최대 토큰 수
     * @param refillRate 새로운 재충전 속도
     * @author crawlinghub
     */
    public void updateBucketConfig(Long userAgentId, int maxTokens, double refillRate) {
        String bucketKey = BUCKET_KEY_PREFIX + userAgentId;

        BucketStatus currentStatus = getBucketStatus(userAgentId);

        if (currentStatus == null) {
            // Bucket이 없으면 초기화와 동시에 설정
            tryConsume(userAgentId, 0, refillRate, maxTokens);
            return;
        }

        // 현재 토큰 수를 새로운 max_tokens로 제한
        double adjustedTokens = Math.min(currentStatus.getCurrentTokens(), maxTokens);

        redisTemplate.opsForHash().put(bucketKey, "tokens", String.valueOf(adjustedTokens));
        redisTemplate.opsForHash().put(bucketKey, "max_tokens", String.valueOf(maxTokens));
        redisTemplate.opsForHash().put(bucketKey, "refill_rate", String.valueOf(refillRate));
    }

    /**
     * Bucket 삭제
     *
     * @param userAgentId User-Agent ID
     * @author crawlinghub
     */
    public void deleteBucket(Long userAgentId) {
        String bucketKey = BUCKET_KEY_PREFIX + userAgentId;
        redisTemplate.delete(bucketKey);
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
