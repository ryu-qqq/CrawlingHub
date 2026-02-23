package com.ryuqq.crawlinghub.adapter.in.scheduler.task;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.ProcessPendingCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverFailedCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverTimeoutCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.port.in.command.ProcessPendingCrawlTaskOutboxUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverFailedCrawlTaskOutboxUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverTimeoutCrawlTaskOutboxUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CrawlTask Outbox 스케줄러
 *
 * <p><strong>용도</strong>: PENDING 아웃박스 처리 + 좀비(PROCESSING 타임아웃) 복구 + FAILED 자동 복구
 *
 * <p><strong>스케줄 구성</strong>:
 *
 * <ul>
 *   <li>{@code processPendingOutboxes} — PENDING 상태 아웃박스를 주기적으로 처리
 *   <li>{@code recoverTimeoutOutboxes} — PROCESSING 상태에서 타임아웃된 좀비 아웃박스를 PENDING으로 복원
 *   <li>{@code recoverFailedOutboxes} — FAILED 상태에서 일정 시간 경과한 아웃박스를 PENDING으로 복원
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.crawl-task-outbox.process-pending",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class CrawlTaskOutboxScheduler {

    private final ProcessPendingCrawlTaskOutboxUseCase processPendingUseCase;
    private final RecoverTimeoutCrawlTaskOutboxUseCase recoverTimeoutUseCase;
    private final RecoverFailedCrawlTaskOutboxUseCase recoverFailedUseCase;
    private final SchedulerProperties.CrawlTaskOutbox config;

    public CrawlTaskOutboxScheduler(
            ProcessPendingCrawlTaskOutboxUseCase processPendingUseCase,
            RecoverTimeoutCrawlTaskOutboxUseCase recoverTimeoutUseCase,
            RecoverFailedCrawlTaskOutboxUseCase recoverFailedUseCase,
            SchedulerProperties properties) {
        this.processPendingUseCase = processPendingUseCase;
        this.recoverTimeoutUseCase = recoverTimeoutUseCase;
        this.recoverFailedUseCase = recoverFailedUseCase;
        this.config = properties.jobs().crawlTaskOutbox();
    }

    /**
     * PENDING 상태 아웃박스 배치 처리
     *
     * <p>delaySeconds 이상 경과한 PENDING 아웃박스를 조회하여 SQS 발행
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.crawl-task-outbox.process-pending.cron}",
            zone = "${scheduler.jobs.crawl-task-outbox.process-pending.timezone}")
    @SchedulerJob("CrawlTaskOutbox-ProcessPending")
    public SchedulerBatchProcessingResult processPendingOutboxes() {
        SchedulerProperties.ProcessPending pending = config.processPending();

        ProcessPendingCrawlTaskOutboxCommand command =
                ProcessPendingCrawlTaskOutboxCommand.of(
                        pending.batchSize(), pending.delaySeconds());

        return processPendingUseCase.execute(command);
    }

    /**
     * PROCESSING 타임아웃 좀비 아웃박스 복구
     *
     * <p>timeoutSeconds 이상 PROCESSING 상태인 아웃박스를 PENDING으로 복원
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.crawl-task-outbox.recover-timeout.cron}",
            zone = "${scheduler.jobs.crawl-task-outbox.recover-timeout.timezone}")
    @SchedulerJob("CrawlTaskOutbox-RecoverTimeout")
    public SchedulerBatchProcessingResult recoverTimeoutOutboxes() {
        SchedulerProperties.RecoverTimeout timeout = config.recoverTimeout();

        RecoverTimeoutCrawlTaskOutboxCommand command =
                RecoverTimeoutCrawlTaskOutboxCommand.of(
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
            cron = "${scheduler.jobs.crawl-task-outbox.recover-failed.cron}",
            zone = "${scheduler.jobs.crawl-task-outbox.recover-failed.timezone}")
    @SchedulerJob("CrawlTaskOutbox-RecoverFailed")
    public SchedulerBatchProcessingResult recoverFailedOutboxes() {
        SchedulerProperties.RecoverFailed recoverFailed = config.recoverFailed();

        RecoverFailedCrawlTaskOutboxCommand command =
                RecoverFailedCrawlTaskOutboxCommand.of(
                        recoverFailed.batchSize(), recoverFailed.delaySeconds());

        return recoverFailedUseCase.execute(command);
    }
}
