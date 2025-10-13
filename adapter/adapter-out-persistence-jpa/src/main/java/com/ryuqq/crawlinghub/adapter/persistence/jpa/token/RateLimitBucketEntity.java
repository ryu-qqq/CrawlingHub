package com.ryuqq.crawlinghub.adapter.persistence.jpa.token;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Rate Limit Bucket Entity
 * Redis Token Bucket 상태를 DB에 동기화
 *
 * @author crawlinghub
 */
@Entity
@Table(name = "rate_limit_bucket",
    indexes = {
        @Index(name = "idx_user_agent_id", columnList = "user_agent_id"),
        @Index(name = "idx_last_synced_at", columnList = "last_synced_at")
    }
)
public class RateLimitBucketEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bucket_id")
    private Long bucketId;

    @Column(name = "user_agent_id", nullable = false, unique = true)
    private Long userAgentId;

    @Column(name = "current_tokens", nullable = false)
    private double currentTokens;

    @Column(name = "max_tokens", nullable = false)
    private int maxTokens;

    @Column(name = "refill_rate", nullable = false)
    private double refillRate;

    @Column(name = "last_refill_timestamp", nullable = false)
    private long lastRefillTimestamp;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Column(name = "total_requests", nullable = false)
    private long totalRequests = 0;

    @Column(name = "total_rejected", nullable = false)
    private long totalRejected = 0;

    protected RateLimitBucketEntity() {
    }

    private RateLimitBucketEntity(
            Long userAgentId,
            double currentTokens,
            int maxTokens,
            double refillRate,
            long lastRefillTimestamp) {
        this.userAgentId = userAgentId;
        this.currentTokens = currentTokens;
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.lastRefillTimestamp = lastRefillTimestamp;
        this.lastSyncedAt = LocalDateTime.now();
    }

    /**
     * Redis 상태로부터 업데이트
     */
    public void updateFromRedis(
            double currentTokens,
            int maxTokens,
            double refillRate,
            long lastRefillTimestamp) {
        this.currentTokens = currentTokens;
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.lastRefillTimestamp = lastRefillTimestamp;
        this.lastSyncedAt = LocalDateTime.now();
    }

    /**
     * 요청 통계 업데이트
     */
    public void recordRequest(boolean allowed) {
        this.totalRequests++;
        if (!allowed) {
            this.totalRejected++;
        }
    }

    /**
     * 거부율 계산
     */
    public double getRejectionRate() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return (double) totalRejected / totalRequests;
    }

    public Long getBucketId() {
        return bucketId;
    }

    public Long getUserAgentId() {
        return userAgentId;
    }

    public double getCurrentTokens() {
        return currentTokens;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public double getRefillRate() {
        return refillRate;
    }

    public long getLastRefillTimestamp() {
        return lastRefillTimestamp;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getTotalRejected() {
        return totalRejected;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userAgentId;
        private double currentTokens;
        private int maxTokens;
        private double refillRate;
        private long lastRefillTimestamp;

        public Builder userAgentId(Long userAgentId) {
            this.userAgentId = userAgentId;
            return this;
        }

        public Builder currentTokens(double currentTokens) {
            this.currentTokens = currentTokens;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder refillRate(double refillRate) {
            this.refillRate = refillRate;
            return this;
        }

        public Builder lastRefillTimestamp(long lastRefillTimestamp) {
            this.lastRefillTimestamp = lastRefillTimestamp;
            return this;
        }

        public RateLimitBucketEntity build() {
            return new RateLimitBucketEntity(
                userAgentId,
                currentTokens,
                maxTokens,
                refillRate,
                lastRefillTimestamp
            );
        }
    }
}
