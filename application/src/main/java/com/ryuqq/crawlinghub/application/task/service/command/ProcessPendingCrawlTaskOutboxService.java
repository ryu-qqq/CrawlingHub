package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.ProcessPendingCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.internal.CrawlTaskOutboxProcessor;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.ProcessPendingCrawlTaskOutboxUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * PENDING 상태의 CrawlTask 아웃박스 처리 Service
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessPendingCrawlTaskOutboxService implements ProcessPendingCrawlTaskOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessPendingCrawlTaskOutboxService.class);

    private final CrawlTaskOutboxReadManager outboxReadManager;
    private final CrawlTaskOutboxProcessor processor;

    public ProcessPendingCrawlTaskOutboxService(
            CrawlTaskOutboxReadManager outboxReadManager, CrawlTaskOutboxProcessor processor) {
        this.outboxReadManager = outboxReadManager;
        this.processor = processor;
    }

    @Override
    public SchedulerBatchProcessingResult execute(ProcessPendingCrawlTaskOutboxCommand command) {
        List<CrawlTaskOutbox> outboxes =
                outboxReadManager.findPendingOlderThan(command.batchSize(), command.delaySeconds());

        if (outboxes.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("CrawlTask 아웃박스 처리 시작: {} 건", outboxes.size());

        int success = 0;
        int failed = 0;

        for (CrawlTaskOutbox outbox : outboxes) {
            boolean result = processor.processOutbox(outbox);
            if (result) {
                success++;
            } else {
                failed++;
            }
        }

        log.info("CrawlTask 아웃박스 처리 완료: 성공={}, 실패={}", success, failed);
        return SchedulerBatchProcessingResult.of(outboxes.size(), success, failed);
    }
}
