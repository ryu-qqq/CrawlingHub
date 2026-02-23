package com.ryuqq.crawlinghub.application.product.internal;

import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncMessageManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawledProductSyncOutbox 개별 항목 처리 Processor
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>PENDING → PROCESSING 전환 (선점)
 *   <li>SQS 메시지 발행
 *   <li>성공 시 SENT, 실패 시 FAILED 상태 변경
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductSyncOutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(CrawledProductSyncOutboxProcessor.class);

    private final CrawledProductSyncOutboxCommandManager commandManager;
    private final CrawledProductSyncMessageManager messageManager;

    public CrawledProductSyncOutboxProcessor(
            CrawledProductSyncOutboxCommandManager commandManager,
            CrawledProductSyncMessageManager messageManager) {
        this.commandManager = commandManager;
        this.messageManager = messageManager;
    }

    /**
     * 아웃박스 항목 처리
     *
     * @param outbox 처리할 아웃박스
     * @return 처리 성공 여부
     */
    public boolean processOutbox(CrawledProductSyncOutbox outbox) {
        try {
            // 1. PENDING → PROCESSING 전환 (선점)
            commandManager.markAsProcessing(outbox);

            // 2. SQS 메시지 발행
            messageManager.publish(outbox);

            // 3. SENT 처리
            commandManager.markAsSent(outbox);

            log.debug(
                    "CrawledProductSyncOutbox 처리 성공: outboxId={}, productId={}",
                    outbox.getId(),
                    outbox.getCrawledProductIdValue());
            return true;
        } catch (Exception e) {
            // 실패 처리
            commandManager.markAsFailed(outbox, "SQS 발행 실패: " + e.getMessage());

            log.warn(
                    "CrawledProductSyncOutbox 처리 실패: outboxId={}, productId={}, error={}",
                    outbox.getId(),
                    outbox.getCrawledProductIdValue(),
                    e.getMessage());
            return false;
        }
    }
}
