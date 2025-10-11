package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.domain.common.BackoffStrategy;
import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "site_retry_policy")
public class SiteRetryPolicyEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "retry_policy_id")
    private Long retryPolicyId;

    @Column(name = "site_id", nullable = false, unique = true)
    private Long siteId;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Enumerated(EnumType.STRING)
    @Column(name = "backoff_strategy", nullable = false, length = 50)
    private BackoffStrategy backoffStrategy;

    @Column(name = "backoff_initial_delay_ms", nullable = false)
    private Integer backoffInitialDelayMs;

    @Column(name = "backoff_max_delay_ms", nullable = false)
    private Integer backoffMaxDelayMs;

    @Column(name = "backoff_multiplier")
    private Double backoffMultiplier;

    @Column(name = "retry_on_status_codes", columnDefinition = "JSON")
    private String retryOnStatusCodes;

    protected SiteRetryPolicyEntity() {
    }

    private SiteRetryPolicyEntity(Long retryPolicyId, Long siteId, Integer maxRetries, BackoffStrategy backoffStrategy,
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

    public void updateRetryCount(Integer maxRetries) {
        this.maxRetries = maxRetries;
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

        public SiteRetryPolicyEntity build() {
            return new SiteRetryPolicyEntity(retryPolicyId, siteId, maxRetries, backoffStrategy,
                                      backoffInitialDelayMs, backoffMaxDelayMs, backoffMultiplier,
                                      retryOnStatusCodes);
        }
    }

}
