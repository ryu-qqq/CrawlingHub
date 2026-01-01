package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.task.dto.response.RepublishResultResponse;
import com.ryuqq.crawlinghub.application.task.manager.command.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.messaging.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskOutboxReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.command.RepublishOutboxUseCase;
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
 * <p><strong>책임</strong>: PENDING/FAILED 상태의 Outbox를 SQS로 재발행
 *
 * <p><strong>의존성</strong>:
 *
 * <ul>
 *   <li>CrawlTaskOutboxReadManager: Outbox 조회
 *   <li>CrawlTaskOutboxTransactionManager: Outbox 상태 변경
 *   <li>CrawlTaskMessageManager: SQS 메시지 발행
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RepublishOutboxService implements RepublishOutboxUseCase {

    private static final Logger log = LoggerFactory.getLogger(RepublishOutboxService.class);

    private final CrawlTaskOutboxReadManager outboxReadManager;
    private final CrawlTaskOutboxTransactionManager outboxTransactionManager;
    private final CrawlTaskMessageManager messageManager;

    public RepublishOutboxService(
            CrawlTaskOutboxReadManager outboxReadManager,
            CrawlTaskOutboxTransactionManager outboxTransactionManager,
            CrawlTaskMessageManager messageManager) {
        this.outboxReadManager = outboxReadManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.messageManager = messageManager;
    }

    @Override
    public RepublishResultResponse republish(Long crawlTaskId) {
        CrawlTaskId taskId = CrawlTaskId.of(crawlTaskId);

        Optional<CrawlTaskOutbox> outboxOptional = outboxReadManager.findByCrawlTaskId(taskId);
        if (outboxOptional.isEmpty()) {
            return RepublishResultResponse.failure(
                    crawlTaskId, "Outbox를 찾을 수 없습니다. Task ID: " + crawlTaskId);
        }

        CrawlTaskOutbox outbox = outboxOptional.get();

        if (outbox.isSent()) {
            return RepublishResultResponse.failure(crawlTaskId, "이미 발행 완료된 Outbox입니다.");
        }

        try {
            messageManager.publishFromOutbox(outbox);
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
