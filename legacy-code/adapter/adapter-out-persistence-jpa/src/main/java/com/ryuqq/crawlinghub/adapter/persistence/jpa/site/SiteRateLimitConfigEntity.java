package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.domain.common.BackoffStrategy;
import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "site_rate_limit_config")
public class SiteRateLimitConfigEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_limit_config_id")
    private Long rateLimitConfigId;

    @Column(name = "site_id", nullable = false, unique = true)
    private Long siteId;

    @Column(name = "requests_per_second")
    private Integer requestsPerSecond;

    @Column(name = "requests_per_minute")
    private Integer requestsPerMinute;

    @Column(name = "requests_per_hour")
    private Integer requestsPerHour;

    @Column(name = "concurrent_requests")
    private Integer concurrentRequests;

    @Enumerated(EnumType.STRING)
    @Column(name = "backoff_strategy", length = 50)
    private BackoffStrategy backoffStrategy;

    @Column(name = "backoff_initial_delay_ms")
    private Integer backoffInitialDelayMs;

    @Column(name = "backoff_max_delay_ms")
    private Integer backoffMaxDelayMs;

    protected SiteRateLimitConfigEntity() {
    }

    private SiteRateLimitConfigEntity(Long rateLimitConfigId, Long siteId, Integer requestsPerSecond,
                               Integer requestsPerMinute, Integer requestsPerHour, Integer concurrentRequests,
                               BackoffStrategy backoffStrategy, Integer backoffInitialDelayMs,
                               Integer backoffMaxDelayMs) {
        this.rateLimitConfigId = rateLimitConfigId;
        this.siteId = siteId;
        this.requestsPerSecond = requestsPerSecond;
        this.requestsPerMinute = requestsPerMinute;
        this.requestsPerHour = requestsPerHour;
        this.concurrentRequests = concurrentRequests;
        this.backoffStrategy = backoffStrategy;
        this.backoffInitialDelayMs = backoffInitialDelayMs;
        this.backoffMaxDelayMs = backoffMaxDelayMs;
    }

    public Long getRateLimitConfigId() {
        return rateLimitConfigId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public Integer getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public Integer getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public Integer getRequestsPerHour() {
        return requestsPerHour;
    }

    public Integer getConcurrentRequests() {
        return concurrentRequests;
    }

    public BackoffStrategy getBackoffStrategy() {
        return backoffStrategy;
    }

    public Integer getBackoffInitialDelayMs() {
        return backoffInitialDelayMs;
    }

    public Integer getBackoffMaxDelayMs() {
        return backoffMaxDelayMs;
    }

    public void updateRateLimits(Integer perSecond, Integer perMinute, Integer perHour) {
        this.requestsPerSecond = perSecond;
        this.requestsPerMinute = perMinute;
        this.requestsPerHour = perHour;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long rateLimitConfigId;
        private Long siteId;
        private Integer requestsPerSecond;
        private Integer requestsPerMinute;
        private Integer requestsPerHour;
        private Integer concurrentRequests;
        private BackoffStrategy backoffStrategy;
        private Integer backoffInitialDelayMs;
        private Integer backoffMaxDelayMs;

        public Builder rateLimitConfigId(Long rateLimitConfigId) {
            this.rateLimitConfigId = rateLimitConfigId;
            return this;
        }

        public Builder siteId(Long siteId) {
            this.siteId = siteId;
            return this;
        }

        public Builder requestsPerSecond(Integer requestsPerSecond) {
            this.requestsPerSecond = requestsPerSecond;
            return this;
        }

        public Builder requestsPerMinute(Integer requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
            return this;
        }

        public Builder requestsPerHour(Integer requestsPerHour) {
            this.requestsPerHour = requestsPerHour;
            return this;
        }

        public Builder concurrentRequests(Integer concurrentRequests) {
            this.concurrentRequests = concurrentRequests;
            return this;
        }

        public Builder backoffStrategy(BackoffStrategy backoffStrategy) {
            this.backoffStrategy = backoffStrategy;
            return this;
        }

        public Builder backoffInitialDelayMs(Integer backoffInitialDelayMs) {
            this.backoffInitialDelayMs = backoffInitialDelayMs;
            return this;
        }

        public Builder backoffMaxDelayMs(Integer backoffMaxDelayMs) {
            this.backoffMaxDelayMs = backoffMaxDelayMs;
            return this;
        }

        public SiteRateLimitConfigEntity build() {
            return new SiteRateLimitConfigEntity(rateLimitConfigId, siteId, requestsPerSecond, requestsPerMinute,
                                          requestsPerHour, concurrentRequests, backoffStrategy,
                                          backoffInitialDelayMs, backoffMaxDelayMs);
        }
    }

}
