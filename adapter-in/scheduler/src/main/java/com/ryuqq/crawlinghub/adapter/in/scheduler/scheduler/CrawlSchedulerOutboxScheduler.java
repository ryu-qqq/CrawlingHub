package com.ryuqq.crawlinghub.adapter.in.scheduler.scheduler;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.schedule.dto.command.ProcessPendingSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RecoverTimeoutSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.ProcessPendingSchedulerOutboxUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RecoverTimeoutSchedulerOutboxUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler Outbox 스케줄러
 *
 * <p><strong>용도</strong>: PENDING 아웃박스 처리 + 좀비(PROCESSING 타임아웃) 복구
 *
 * <p><strong>스케줄 구성</strong>:
 *
 * <ul>
 *   <li>{@code processPendingOutboxes} — PENDING 상태 아웃박스를 주기적으로 처리
 *   <li>{@code recoverTimeoutOutboxes} — PROCESSING 상태에서 타임아웃된 좀비 아웃박스를 PENDING으로 복원
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.crawl-scheduler-outbox.process-pending",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class CrawlSchedulerOutboxScheduler {

    private final ProcessPendingSchedulerOutboxUseCase processPendingOutboxUseCase;
    private final RecoverTimeoutSchedulerOutboxUseCase recoverTimeoutOutboxUseCase;
    private final SchedulerProperties.CrawlSchedulerOutbox config;

    public CrawlSchedulerOutboxScheduler(
            ProcessPendingSchedulerOutboxUseCase processPendingOutboxUseCase,
            RecoverTimeoutSchedulerOutboxUseCase recoverTimeoutOutboxUseCase,
            SchedulerProperties properties) {
        this.processPendingOutboxUseCase = processPendingOutboxUseCase;
        this.recoverTimeoutOutboxUseCase = recoverTimeoutOutboxUseCase;
        this.config = properties.jobs().crawlSchedulerOutbox();
    }

    /**
     * PENDING 상태 아웃박스 배치 처리
     *
     * <p>delaySeconds 이상 경과한 PENDING 아웃박스를 조회하여 EventBridge 동기화
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.crawl-scheduler-outbox.process-pending.cron}",
            zone = "${scheduler.jobs.crawl-scheduler-outbox.process-pending.timezone}")
    @SchedulerJob("CrawlSchedulerOutbox-ProcessPending")
    public SchedulerBatchProcessingResult processPendingOutboxes() {
        SchedulerProperties.ProcessPending pending = config.processPending();

        ProcessPendingSchedulerOutboxCommand command =
                ProcessPendingSchedulerOutboxCommand.of(
                        pending.batchSize(), pending.delaySeconds());

        return processPendingOutboxUseCase.execute(command);
    }

    /**
     * PROCESSING 타임아웃 좀비 아웃박스 복구
     *
     * <p>timeoutSeconds 이상 PROCESSING 상태인 아웃박스를 PENDING으로 복원
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.crawl-scheduler-outbox.recover-timeout.cron}",
            zone = "${scheduler.jobs.crawl-scheduler-outbox.recover-timeout.timezone}")
    @SchedulerJob("CrawlSchedulerOutbox-RecoverTimeout")
    public SchedulerBatchProcessingResult recoverTimeoutOutboxes() {
        SchedulerProperties.RecoverTimeout timeout = config.recoverTimeout();

        RecoverTimeoutSchedulerOutboxCommand command =
                RecoverTimeoutSchedulerOutboxCommand.of(
                        timeout.batchSize(), timeout.timeoutSeconds());

        return recoverTimeoutOutboxUseCase.execute(command);
    }
}
