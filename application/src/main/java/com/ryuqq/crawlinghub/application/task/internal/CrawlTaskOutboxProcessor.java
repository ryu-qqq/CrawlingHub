package com.ryuqq.crawlinghub.application.task.internal;

import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 아웃박스 개별 항목 처리 Processor
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>PENDING → PROCESSING 전환 (선점)
 *   <li>CrawlTask 상태를 PUBLISHED로 변경
 *   <li>SQS 메시지 발행
 *   <li>성공 시 SENT, 실패 시 FAILED 상태 변경
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskOutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskOutboxProcessor.class);

    private final CrawlTaskCommandManager commandManager;
    private final CrawlTaskOutboxCommandManager outboxCommandManager;
    private final CrawlTaskReadManager readManager;
    private final CrawlTaskMessageManager messageManager;

    public CrawlTaskOutboxProcessor(
            CrawlTaskCommandManager commandManager,
            CrawlTaskOutboxCommandManager outboxCommandManager,
            CrawlTaskReadManager readManager,
            CrawlTaskMessageManager messageManager) {
        this.commandManager = commandManager;
        this.outboxCommandManager = outboxCommandManager;
        this.readManager = readManager;
        this.messageManager = messageManager;
    }

    /**
     * 아웃박스 항목 처리
     *
     * @param outbox 처리할 아웃박스
     * @return 처리 성공 여부
     */
    public boolean processOutbox(CrawlTaskOutbox outbox) {
        try {
            // 1. PENDING → PROCESSING 전환
            outbox.markAsProcessing(Instant.now());
            outboxCommandManager.persist(outbox);

            // 2. CrawlTask 상태 → PUBLISHED 전환 (WAITING 또는 RETRY)
            readManager
                    .findById(outbox.getCrawlTaskId())
                    .ifPresent(
                            task -> {
                                CrawlTaskStatus status = task.getStatus();
                                if (status == CrawlTaskStatus.WAITING) {
                                    task.markAsPublished(Instant.now());
                                    commandManager.persist(task);
                                } else if (status == CrawlTaskStatus.RETRY) {
                                    task.markAsPublishedAfterRetry(Instant.now());
                                    commandManager.persist(task);
                                }
                            });

            // 3. SQS 메시지 발행
            messageManager.publishFromOutbox(outbox);

            // 4. SENT 처리
            outbox.markAsSent(Instant.now());
            outboxCommandManager.persist(outbox);

            log.debug("아웃박스 처리 성공: taskId={}", outbox.getCrawlTaskIdValue());
            return true;
        } catch (Exception e) {
            // 실패 처리
            outbox.markAsFailed(Instant.now());
            outboxCommandManager.persist(outbox);

            log.error(
                    "아웃박스 처리 실패: taskId={}, error={}",
                    outbox.getCrawlTaskIdValue(),
                    e.getMessage());
            return false;
        }
    }
}
