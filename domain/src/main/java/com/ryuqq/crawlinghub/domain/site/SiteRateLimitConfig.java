package com.ryuqq.crawlinghub.domain.site;

import com.ryuqq.crawlinghub.domain.common.BackoffStrategy;

public class SiteRateLimitConfig {

    private final Long rateLimitConfigId;
    private final Long siteId;
    private Integer requestsPerSecond;
    private Integer requestsPerMinute;
    private Integer requestsPerHour;
    private final Integer concurrentRequests;
    private final BackoffStrategy backoffStrategy;
    private final Integer backoffInitialDelayMs;
    private final Integer backoffMaxDelayMs;

    private SiteRateLimitConfig(Long rateLimitConfigId, Long siteId, Integer requestsPerSecond,
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

    public void updateRateLimits(Integer perSecond, Integer perMinute, Integer perHour) {
        this.requestsPerSecond = perSecond;
        this.requestsPerMinute = perMinute;
        this.requestsPerHour = perHour;
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

        public SiteRateLimitConfig build() {
            return new SiteRateLimitConfig(rateLimitConfigId, siteId, requestsPerSecond, requestsPerMinute,
                                          requestsPerHour, concurrentRequests, backoffStrategy,
                                          backoffInitialDelayMs, backoffMaxDelayMs);
        }
    }

}
