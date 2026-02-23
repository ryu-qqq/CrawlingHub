package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.PublishPendingSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductSyncOutboxProcessor;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.PublishPendingSyncOutboxUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * PENDING/FAILED CrawledProductSyncOutbox SQS 발행 Service
 *
 * <p>재시도 가능한 CrawledProductSyncOutbox를 조회하여 Processor에 위임합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class PublishPendingSyncOutboxService implements PublishPendingSyncOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(PublishPendingSyncOutboxService.class);

    private final CrawledProductSyncOutboxReadManager readManager;
    private final CrawledProductSyncOutboxProcessor processor;

    public PublishPendingSyncOutboxService(
            CrawledProductSyncOutboxReadManager readManager,
            CrawledProductSyncOutboxProcessor processor) {
        this.readManager = readManager;
        this.processor = processor;
    }

    @Override
    public SchedulerBatchProcessingResult execute(PublishPendingSyncOutboxCommand command) {
        List<CrawledProductSyncOutbox> outboxes =
                readManager.findRetryableOutboxes(command.maxRetryCount(), command.batchSize());

        if (outboxes.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("CrawledProductSyncOutbox SQS 발행 시작: {} 건", outboxes.size());

        int success = 0;
        int failed = 0;

        for (CrawledProductSyncOutbox outbox : outboxes) {
            if (processor.processOutbox(outbox)) {
                success++;
            } else {
                failed++;
            }
        }

        log.info("CrawledProductSyncOutbox SQS 발행 완료: 성공={}, 실패={}", success, failed);
        return SchedulerBatchProcessingResult.of(outboxes.size(), success, failed);
    }
}
