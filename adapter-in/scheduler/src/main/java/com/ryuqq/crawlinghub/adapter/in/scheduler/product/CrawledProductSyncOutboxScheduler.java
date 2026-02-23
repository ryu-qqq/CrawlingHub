package com.ryuqq.crawlinghub.adapter.in.scheduler.product;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.PublishPendingSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverFailedProductSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverTimeoutProductSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.port.in.command.PublishPendingSyncOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.command.RecoverFailedProductSyncOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.command.RecoverTimeoutProductSyncOutboxUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CrawledProduct CrawledProductSyncOutbox 스케줄러
 *
 * <p><strong>용도</strong>: PENDING CrawledProductSyncOutbox SQS 발행 + 좀비(PROCESSING 타임아웃) 복구 + FAILED
 * 자동 복구
 *
 * <p><strong>스케줄 구성</strong>:
 *
 * <ul>
 *   <li>{@code publishPendingOutboxes} — PENDING/FAILED 상태 CrawledProductSyncOutbox를 SQS로 발행
 *   <li>{@code recoverTimeoutOutboxes} — PROCESSING 상태에서 타임아웃된 좀비 아웃박스를 PENDING으로 복원
 *   <li>{@code recoverFailedOutboxes} — FAILED 상태에서 일정 시간 경과한 아웃박스를 PENDING으로 복원
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.sync-outbox.publish-pending",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class CrawledProductSyncOutboxScheduler {

    private final PublishPendingSyncOutboxUseCase publishPendingUseCase;
    private final RecoverTimeoutProductSyncOutboxUseCase recoverTimeoutUseCase;
    private final RecoverFailedProductSyncOutboxUseCase recoverFailedUseCase;
    private final SchedulerProperties.CrawledProductSyncOutbox config;

    public CrawledProductSyncOutboxScheduler(
            PublishPendingSyncOutboxUseCase publishPendingUseCase,
            RecoverTimeoutProductSyncOutboxUseCase recoverTimeoutUseCase,
            RecoverFailedProductSyncOutboxUseCase recoverFailedUseCase,
            SchedulerProperties properties) {
        this.publishPendingUseCase = publishPendingUseCase;
        this.recoverTimeoutUseCase = recoverTimeoutUseCase;
        this.recoverFailedUseCase = recoverFailedUseCase;
        this.config = properties.jobs().syncOutbox();
    }

    /**
     * PENDING/FAILED CrawledProductSyncOutbox SQS 발행
     *
     * <p>재시도 횟수 제한 내의 PENDING 또는 FAILED Outbox를 SQS로 발행
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.sync-outbox.publish-pending.cron}",
            zone = "${scheduler.jobs.sync-outbox.publish-pending.timezone}")
    @SchedulerJob("CrawledProductSyncOutbox-PublishPending")
    public SchedulerBatchProcessingResult publishPendingOutboxes() {
        SchedulerProperties.CrawledProductSyncOutboxPublishPending publishPending =
                config.publishPending();

        PublishPendingSyncOutboxCommand command =
                PublishPendingSyncOutboxCommand.of(
                        publishPending.batchSize(), publishPending.maxRetryCount());

        return publishPendingUseCase.execute(command);
    }

    /**
     * PROCESSING 타임아웃 좀비 아웃박스 복구
     *
     * <p>timeoutSeconds 이상 PROCESSING 상태인 아웃박스를 PENDING으로 복원
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.sync-outbox.recover-timeout.cron}",
            zone = "${scheduler.jobs.sync-outbox.recover-timeout.timezone}")
    @SchedulerJob("CrawledProductSyncOutbox-RecoverTimeout")
    public SchedulerBatchProcessingResult recoverTimeoutOutboxes() {
        SchedulerProperties.RecoverTimeout timeout = config.recoverTimeout();

        RecoverTimeoutProductSyncOutboxCommand command =
                RecoverTimeoutProductSyncOutboxCommand.of(
                        timeout.batchSize(), timeout.timeoutSeconds());

        return recoverTimeoutUseCase.execute(command);
    }

    /**
     * FAILED 상태 아웃박스 자동 복구
     *
     * <p>delaySeconds 이상 FAILED 상태인 아웃박스를 PENDING으로 복원하여 자동 재처리
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.sync-outbox.recover-failed.cron}",
            zone = "${scheduler.jobs.sync-outbox.recover-failed.timezone}")
    @SchedulerJob("CrawledProductSyncOutbox-RecoverFailed")
    public SchedulerBatchProcessingResult recoverFailedOutboxes() {
        SchedulerProperties.RecoverFailed recoverFailed = config.recoverFailed();

        RecoverFailedProductSyncOutboxCommand command =
                RecoverFailedProductSyncOutboxCommand.of(
                        recoverFailed.batchSize(), recoverFailed.delaySeconds());

        return recoverFailedUseCase.execute(command);
    }
}
