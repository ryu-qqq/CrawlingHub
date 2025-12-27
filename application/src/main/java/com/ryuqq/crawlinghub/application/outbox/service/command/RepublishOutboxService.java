package com.ryuqq.crawlinghub.application.outbox.service.command;

import com.ryuqq.crawlinghub.application.outbox.dto.response.RepublishResultResponse;
import com.ryuqq.crawlinghub.application.outbox.port.in.command.RepublishOutboxUseCase;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.port.out.messaging.CrawlTaskMessagePort;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Outbox 재발행 Service
 *
 * <p>RepublishOutboxUseCase 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RepublishOutboxService implements RepublishOutboxUseCase {

    private static final Logger log = LoggerFactory.getLogger(RepublishOutboxService.class);

    private final CrawlTaskOutboxQueryPort outboxQueryPort;
    private final CrawlTaskOutboxTransactionManager outboxTransactionManager;
    private final CrawlTaskMessagePort messagePort;

    public RepublishOutboxService(
            CrawlTaskOutboxQueryPort outboxQueryPort,
            CrawlTaskOutboxTransactionManager outboxTransactionManager,
            CrawlTaskMessagePort messagePort) {
        this.outboxQueryPort = outboxQueryPort;
        this.outboxTransactionManager = outboxTransactionManager;
        this.messagePort = messagePort;
    }

    @Override
    public RepublishResultResponse republish(Long crawlTaskId) {
        CrawlTaskId taskId = CrawlTaskId.of(crawlTaskId);

        Optional<CrawlTaskOutbox> outboxOptional = outboxQueryPort.findByCrawlTaskId(taskId);
        if (outboxOptional.isEmpty()) {
            return RepublishResultResponse.failure(
                    crawlTaskId, "Outbox를 찾을 수 없습니다. Task ID: " + crawlTaskId);
        }

        CrawlTaskOutbox outbox = outboxOptional.get();

        if (outbox.isSent()) {
            return RepublishResultResponse.failure(crawlTaskId, "이미 발행 완료된 Outbox입니다.");
        }

        try {
            messagePort.publishFromOutbox(outbox);
            outboxTransactionManager.markAsSent(outbox);

            log.info("Outbox 재발행 성공: taskId={}", crawlTaskId);
            return RepublishResultResponse.success(crawlTaskId);
        } catch (Exception e) {
            log.error("Outbox 재발행 실패: taskId={}", crawlTaskId, e);
            outboxTransactionManager.markAsFailed(outbox);

            return RepublishResultResponse.failure(crawlTaskId, "SQS 발행 실패: " + e.getMessage());
        }
    }
}
