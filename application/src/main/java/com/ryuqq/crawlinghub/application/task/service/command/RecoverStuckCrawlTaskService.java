package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.common.metric.annotation.CrawlMetric;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverStuckCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverStuckCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * RUNNING 고아 CrawlTask 복구 Service
 *
 * <p><strong>용도</strong>: RUNNING 상태에서 일정 시간 이상 머물러있는 고아 CrawlTask를 FAILED로 마킹 후 재시도 가능하면 자동 재시도
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>RUNNING 상태 + updatedAt이 timeoutSeconds 이전인 CrawlTask 조회
 *   <li>각 task에 대해 markAsFailed(now) → RUNNING → FAILED
 *   <li>canRetry() 체크:
 *       <ul>
 *         <li>true: attemptRetry(now) → FAILED → RETRY + retryCount++ → outbox.resetToPending()
 *         <li>false: FAILED 유지 (재시도 불가)
 *       </ul>
 *   <li>persist
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverStuckCrawlTaskService implements RecoverStuckCrawlTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecoverStuckCrawlTaskService.class);

    private final CrawlTaskReadManager readManager;
    private final CrawlTaskCommandManager commandManager;
    private final TimeProvider timeProvider;

    public RecoverStuckCrawlTaskService(
            CrawlTaskReadManager readManager,
            CrawlTaskCommandManager commandManager,
            TimeProvider timeProvider) {
        this.readManager = readManager;
        this.commandManager = commandManager;
        this.timeProvider = timeProvider;
    }

    @CrawlMetric(value = "crawl_task", operation = "recover_stuck")
    @Override
    public SchedulerBatchProcessingResult execute(RecoverStuckCrawlTaskCommand command) {
        List<CrawlTask> stuckTasks =
                readManager.findRunningOlderThan(command.batchSize(), command.timeoutSeconds());

        if (stuckTasks.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("RUNNING 고아 CrawlTask 복구 시작: {} 건", stuckTasks.size());

        Instant now = timeProvider.now();
        int recovered = 0;
        int failed = 0;

        for (CrawlTask task : stuckTasks) {
            try {
                recoverTask(task, now);
                recovered++;
            } catch (Exception e) {
                failed++;
                log.error(
                        "RUNNING 고아 복구 실패: taskId={}, error={}", task.getIdValue(), e.getMessage());
            }
        }

        log.info("RUNNING 고아 CrawlTask 복구 완료: 복구={}, 실패={}", recovered, failed);
        return SchedulerBatchProcessingResult.of(stuckTasks.size(), recovered, failed);
    }

    private void recoverTask(CrawlTask task, Instant now) {
        // 1. RUNNING → FAILED
        task.markAsFailed(now);

        // 2. 재시도 가능 여부 확인
        if (task.canRetry()) {
            // 2-1. FAILED → RETRY + retryCount++
            task.attemptRetry(now);

            // 2-2. Outbox를 PENDING으로 리셋 → ProcessPending 스케줄러가 재발행
            if (task.hasOutbox()) {
                task.getOutbox().resetToPending();
            }

            log.info(
                    "RUNNING 고아 재시도 예약: taskId={}, retryCount={}",
                    task.getIdValue(),
                    task.getRetryCountValue());
        } else {
            log.warn(
                    "RUNNING 고아 최종 실패 (재시도 불가): taskId={}, retryCount={}",
                    task.getIdValue(),
                    task.getRetryCountValue());
        }

        // 3. 저장
        commandManager.persist(task);
    }
}
