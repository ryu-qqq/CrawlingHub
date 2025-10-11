package com.ryuqq.crawlinghub.domain.site;

import com.ryuqq.crawlinghub.domain.common.BackoffStrategy;

public class SiteRetryPolicy {

    private final Long retryPolicyId;
    private final Long siteId;
    private Integer maxRetries;
    private final BackoffStrategy backoffStrategy;
    private final Integer backoffInitialDelayMs;
    private final Integer backoffMaxDelayMs;
    private final Double backoffMultiplier;
    private final String retryOnStatusCodes;

    private SiteRetryPolicy(Long retryPolicyId, Long siteId, Integer maxRetries, BackoffStrategy backoffStrategy,
                           Integer backoffInitialDelayMs, Integer backoffMaxDelayMs, Double backoffMultiplier,
                           String retryOnStatusCodes) {
        this.retryPolicyId = retryPolicyId;
        this.siteId = siteId;
        this.maxRetries = maxRetries;
        this.backoffStrategy = backoffStrategy;
        this.backoffInitialDelayMs = backoffInitialDelayMs;
        this.backoffMaxDelayMs = backoffMaxDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.retryOnStatusCodes = retryOnStatusCodes;
    }

    public void updateRetryCount(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Long getRetryPolicyId() {
        return retryPolicyId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public Integer getMaxRetries() {
        return maxRetries;
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

    public Double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public String getRetryOnStatusCodes() {
        return retryOnStatusCodes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long retryPolicyId;
        private Long siteId;
        private Integer maxRetries;
        private BackoffStrategy backoffStrategy;
        private Integer backoffInitialDelayMs;
        private Integer backoffMaxDelayMs;
        private Double backoffMultiplier;
        private String retryOnStatusCodes;

        public Builder retryPolicyId(Long retryPolicyId) {
            this.retryPolicyId = retryPolicyId;
            return this;
        }

        public Builder siteId(Long siteId) {
            this.siteId = siteId;
            return this;
        }

        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
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

        public Builder backoffMultiplier(Double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        public Builder retryOnStatusCodes(String retryOnStatusCodes) {
            this.retryOnStatusCodes = retryOnStatusCodes;
            return this;
        }

        public SiteRetryPolicy build() {
            return new SiteRetryPolicy(retryPolicyId, siteId, maxRetries, backoffStrategy,
                                      backoffInitialDelayMs, backoffMaxDelayMs, backoffMultiplier,
                                      retryOnStatusCodes);
        }
    }

}
