package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverTimeoutCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverTimeoutCrawlTaskOutboxUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 타임아웃 CrawlTask 아웃박스 복구 Service
 *
 * <p><strong>용도</strong>: PROCESSING 상태에서 일정 시간 초과된 좀비 아웃박스를 PENDING으로 복원
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverTimeoutCrawlTaskOutboxService implements RecoverTimeoutCrawlTaskOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverTimeoutCrawlTaskOutboxService.class);

    private final CrawlTaskOutboxReadManager outboxReadManager;
    private final CrawlTaskOutboxCommandManager outboxCommandManager;

    public RecoverTimeoutCrawlTaskOutboxService(
            CrawlTaskOutboxReadManager outboxReadManager,
            CrawlTaskOutboxCommandManager outboxCommandManager) {
        this.outboxReadManager = outboxReadManager;
        this.outboxCommandManager = outboxCommandManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(RecoverTimeoutCrawlTaskOutboxCommand command) {
        List<CrawlTaskOutbox> staleOutboxes =
                outboxReadManager.findStaleProcessing(
                        command.batchSize(), command.timeoutSeconds());

        if (staleOutboxes.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("좀비 CrawlTask 아웃박스 복구 시작: {} 건", staleOutboxes.size());

        int success = 0;
        int failed = 0;

        for (CrawlTaskOutbox outbox : staleOutboxes) {
            try {
                outbox.resetToPending();
                outboxCommandManager.persist(outbox);
                success++;
                log.debug("좀비 아웃박스 복구 성공: taskId={}", outbox.getCrawlTaskIdValue());
            } catch (Exception e) {
                failed++;
                log.error(
                        "좀비 아웃박스 복구 실패: taskId={}, error={}",
                        outbox.getCrawlTaskIdValue(),
                        e.getMessage());
            }
        }

        log.info("좀비 CrawlTask 아웃박스 복구 완료: 성공={}, 실패={}", success, failed);
        return SchedulerBatchProcessingResult.of(staleOutboxes.size(), success, failed);
    }
}
