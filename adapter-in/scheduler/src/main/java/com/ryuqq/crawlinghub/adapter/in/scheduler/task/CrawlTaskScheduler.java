package com.ryuqq.crawlinghub.adapter.in.scheduler.task;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverStuckCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverStuckCrawlTaskUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 스케줄러
 *
 * <p><strong>용도</strong>: RUNNING 상태에서 멈춰있는 고아 CrawlTask 자동 복구
 *
 * <p><strong>스케줄 구성</strong>:
 *
 * <ul>
 *   <li>{@code recoverStuckTasks} — timeoutSeconds 이상 RUNNING 상태인 CrawlTask를 FAILED로 마킹 후 재시도 가능하면
 *       자동 재시도
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.crawl-task.recover-stuck",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class CrawlTaskScheduler {

    private final RecoverStuckCrawlTaskUseCase recoverStuckUseCase;
    private final SchedulerProperties.CrawlTask config;

    public CrawlTaskScheduler(
            RecoverStuckCrawlTaskUseCase recoverStuckUseCase, SchedulerProperties properties) {
        this.recoverStuckUseCase = recoverStuckUseCase;
        this.config = properties.jobs().crawlTask();
    }

    /**
     * RUNNING 고아 CrawlTask 복구
     *
     * <p>timeoutSeconds 이상 RUNNING 상태인 CrawlTask를 FAILED로 마킹 후 재시도 가능하면 자동 재시도
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.crawl-task.recover-stuck.cron}",
            zone = "${scheduler.jobs.crawl-task.recover-stuck.timezone}")
    @SchedulerJob("CrawlTask-RecoverStuck")
    public SchedulerBatchProcessingResult recoverStuckTasks() {
        SchedulerProperties.RecoverStuck recoverStuck = config.recoverStuck();

        RecoverStuckCrawlTaskCommand command =
                RecoverStuckCrawlTaskCommand.of(
                        recoverStuck.batchSize(), recoverStuck.timeoutSeconds());

        return recoverStuckUseCase.execute(command);
    }
}
