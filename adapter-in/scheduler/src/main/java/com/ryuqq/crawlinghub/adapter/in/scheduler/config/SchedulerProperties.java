package com.ryuqq.crawlinghub.adapter.in.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 스케줄러 설정 Properties (nested record 패턴)
 *
 * @param jobs 스케줄러 작업 설정
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "scheduler")
public record SchedulerProperties(Jobs jobs) {

    public record Jobs(
            CrawlSchedulerOutbox crawlSchedulerOutbox,
            CrawlTaskOutbox crawlTaskOutbox,
            CrawlTask crawlTask,
            CrawledRawProcessing crawledRawProcessing,
            UserAgentHousekeeper userAgentHousekeeper,
            CrawledProductSyncOutbox syncOutbox) {}

    public record UserAgentHousekeeper(
            boolean enabled,
            int fixedDelayMs,
            int renewalBufferMinutes,
            int maxSessionBatchSize,
            long sessionDelayMillis,
            long leakThresholdMillis,
            int dbSyncInterval) {}

    public record CrawledRawProcessing(
            ProcessCrawledRaw processMiniShop,
            ProcessCrawledRaw processDetail,
            ProcessCrawledRaw processOption) {}

    public record ProcessCrawledRaw(boolean enabled, String cron, String timezone, int batchSize) {}

    public record CrawlTask(RecoverStuck recoverStuck) {}

    public record RecoverStuck(
            boolean enabled, String cron, String timezone, int batchSize, long timeoutSeconds) {}

    public record CrawlTaskOutbox(
            ProcessPending processPending,
            RecoverTimeout recoverTimeout,
            RecoverFailed recoverFailed) {}

    public record CrawlSchedulerOutbox(
            ProcessPending processPending, RecoverTimeout recoverTimeout) {}

    public record ProcessPending(
            boolean enabled, String cron, String timezone, int batchSize, int delaySeconds) {}

    public record RecoverTimeout(
            boolean enabled, String cron, String timezone, int batchSize, long timeoutSeconds) {}

    public record RecoverFailed(
            boolean enabled, String cron, String timezone, int batchSize, int delaySeconds) {}

    public record CrawledProductSyncOutbox(
            CrawledProductSyncOutboxPublishPending publishPending,
            RecoverTimeout recoverTimeout,
            RecoverFailed recoverFailed) {}

    public record CrawledProductSyncOutboxPublishPending(
            boolean enabled, String cron, String timezone, int batchSize, int maxRetryCount) {}
}
