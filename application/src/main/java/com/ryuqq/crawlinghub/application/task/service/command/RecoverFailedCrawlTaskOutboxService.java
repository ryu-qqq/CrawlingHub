package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverFailedCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverFailedCrawlTaskOutboxUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * FAILED CrawlTask 아웃박스 복구 Service
 *
 * <p><strong>용도</strong>: FAILED 상태에서 일정 시간 경과한 아웃박스를 PENDING으로 복원하여 자동 재처리
 *
 * <p><strong>재시도 제한</strong>: CrawlTaskOutbox.canRetry()로 최대 재시도 횟수(3회) 초과 시 복구하지 않음
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverFailedCrawlTaskOutboxService implements RecoverFailedCrawlTaskOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverFailedCrawlTaskOutboxService.class);

    private final CrawlTaskOutboxReadManager outboxReadManager;
    private final CrawlTaskOutboxCommandManager outboxCommandManager;

    public RecoverFailedCrawlTaskOutboxService(
            CrawlTaskOutboxReadManager outboxReadManager,
            CrawlTaskOutboxCommandManager outboxCommandManager) {
        this.outboxReadManager = outboxReadManager;
        this.outboxCommandManager = outboxCommandManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(RecoverFailedCrawlTaskOutboxCommand command) {
        List<CrawlTaskOutbox> failedOutboxes =
                outboxReadManager.findFailedOlderThan(command.batchSize(), command.delaySeconds());

        if (failedOutboxes.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("FAILED CrawlTask 아웃박스 복구 시작: {} 건", failedOutboxes.size());

        int success = 0;
        int failed = 0;

        for (CrawlTaskOutbox outbox : failedOutboxes) {
            try {
                outbox.resetToPending();
                outboxCommandManager.persist(outbox);
                success++;
                log.debug("FAILED 아웃박스 복구 성공: taskId={}", outbox.getCrawlTaskIdValue());
            } catch (Exception e) {
                failed++;
                log.error(
                        "FAILED 아웃박스 복구 실패: taskId={}, error={}",
                        outbox.getCrawlTaskIdValue(),
                        e.getMessage());
            }
        }

        log.info("FAILED CrawlTask 아웃박스 복구 완료: 성공={}, 실패={}", success, failed);
        return SchedulerBatchProcessingResult.of(failedOutboxes.size(), success, failed);
    }
}
