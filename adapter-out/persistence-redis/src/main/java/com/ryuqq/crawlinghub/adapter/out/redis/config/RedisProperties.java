package com.ryuqq.crawlinghub.adapter.out.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 설정 Properties
 *
 * <p><strong>용도</strong>: Redis 연결 및 분산 락 설정 관리
 *
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "distributed-lock")
public class RedisProperties {

    private long defaultWaitTimeMs = 5000;
    private long defaultLeaseTimeMs = 30000;
    private String keyPrefix = "crawlinghub:lock:";
    private CrawlTriggerLock crawlTrigger = new CrawlTriggerLock();
    private CrawlTaskLock crawlTask = new CrawlTaskLock();

    public long getDefaultWaitTimeMs() {
        return defaultWaitTimeMs;
    }

    public void setDefaultWaitTimeMs(long defaultWaitTimeMs) {
        this.defaultWaitTimeMs = defaultWaitTimeMs;
    }

    public long getDefaultLeaseTimeMs() {
        return defaultLeaseTimeMs;
    }

    public void setDefaultLeaseTimeMs(long defaultLeaseTimeMs) {
        this.defaultLeaseTimeMs = defaultLeaseTimeMs;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public CrawlTriggerLock getCrawlTrigger() {
        return crawlTrigger;
    }

    public void setCrawlTrigger(CrawlTriggerLock crawlTrigger) {
        this.crawlTrigger = crawlTrigger;
    }

    public CrawlTaskLock getCrawlTask() {
        return crawlTask;
    }

    public void setCrawlTask(CrawlTaskLock crawlTask) {
        this.crawlTask = crawlTask;
    }

    /** CrawlTrigger 전용 락 설정 */
    public static class CrawlTriggerLock {
        private long waitTimeMs = 3000;
        private long leaseTimeMs = 60000;

        public long getWaitTimeMs() {
            return waitTimeMs;
        }

        public void setWaitTimeMs(long waitTimeMs) {
            this.waitTimeMs = waitTimeMs;
        }

        public long getLeaseTimeMs() {
            return leaseTimeMs;
        }

        public void setLeaseTimeMs(long leaseTimeMs) {
            this.leaseTimeMs = leaseTimeMs;
        }
    }

    /** CrawlTask 전용 락 설정 */
    public static class CrawlTaskLock {
        private long waitTimeMs = 5000;
        private long leaseTimeMs = 300000;

        public long getWaitTimeMs() {
            return waitTimeMs;
        }

        public void setWaitTimeMs(long waitTimeMs) {
            this.waitTimeMs = waitTimeMs;
        }

        public long getLeaseTimeMs() {
            return leaseTimeMs;
        }

        public void setLeaseTimeMs(long leaseTimeMs) {
            this.leaseTimeMs = leaseTimeMs;
        }
    }
}
