package com.ryuqq.crawlinghub.application.product.listener;

import com.ryuqq.crawlinghub.application.sync.manager.command.SyncOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.sync.manager.messaging.ProductSyncMessageManager;
import com.ryuqq.crawlinghub.application.sync.manager.query.SyncOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 외부 서버 동기화 요청 이벤트 리스너
 *
 * <p><strong>용도</strong>: 트랜잭션 커밋 후 SQS로 메시지 발행
 *
 * <p><strong>Transactional Outbox 패턴</strong>:
 *
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후)
 *   <li>SyncOutbox 조회
 *   <li>SQS로 메시지 발행 시도
 *   <li>성공 시: SENT 상태로 전환
 *   <li>실패 시: PENDING 유지 → 스케줄러에서 재처리
 * </ol>
 *
 * <p><strong>실패 복구</strong>: 스케줄러가 PENDING/FAILED 상태 Outbox를 주기적으로 재처리
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ExternalSyncEventListener {

    private static final Logger log = LoggerFactory.getLogger(ExternalSyncEventListener.class);

    private final SyncOutboxReadManager syncOutboxReadManager;
    private final SyncOutboxTransactionManager syncOutboxTransactionManager;
    private final ProductSyncMessageManager messageManager;

    public ExternalSyncEventListener(
            SyncOutboxReadManager syncOutboxReadManager,
            SyncOutboxTransactionManager syncOutboxTransactionManager,
            ProductSyncMessageManager messageManager) {
        this.syncOutboxReadManager = syncOutboxReadManager;
        this.syncOutboxTransactionManager = syncOutboxTransactionManager;
        this.messageManager = messageManager;
    }

    /**
     * 외부 서버 동기화 요청 이벤트 처리
     *
     * <p>트랜잭션 커밋 후 실행됩니다.
     *
     * @param event 외부 서버 동기화 요청 이벤트
     */
    @EventListener
    public void handleExternalSyncRequested(ExternalSyncRequestedEvent event) {
        CrawledProductId productId = event.crawledProductId();
        String idempotencyKey = event.idempotencyKey();

        log.info(
                "외부 서버 동기화 요청 이벤트 수신: productId={}, syncType={}, idempotencyKey={}",
                productId.value(),
                event.syncType(),
                idempotencyKey);

        Optional<CrawledProductSyncOutbox> outboxOpt =
                syncOutboxReadManager.findByIdempotencyKey(idempotencyKey);

        if (outboxOpt.isEmpty()) {
            log.warn(
                    "SyncOutbox를 찾을 수 없습니다: idempotencyKey={}, productId={}",
                    idempotencyKey,
                    productId.value());
            return;
        }

        boolean success = publishToSqs(outboxOpt.get());

        if (success) {
            log.info(
                    "외부 서버 동기화 SQS 발행 완료: productId={}, syncType={}",
                    productId.value(),
                    event.syncType());
        } else {
            log.info(
                    "외부 서버 동기화 SQS 발행 실패 (스케줄러에서 재처리): productId={}, syncType={}",
                    productId.value(),
                    event.syncType());
        }
    }

    /**
     * SQS로 메시지 발행 및 상태 전환
     *
     * @param outbox 발행할 Outbox
     * @return 발행 성공 여부
     */
    private boolean publishToSqs(CrawledProductSyncOutbox outbox) {
        try {
            // SQS 발행
            messageManager.publish(outbox);

            // 발행 성공 → SENT 상태로 전환
            syncOutboxTransactionManager.markAsSent(outbox);

            log.debug(
                    "SQS 발행 성공 → SENT: outboxId={}, productId={}, syncType={}",
                    outbox.getId(),
                    outbox.getCrawledProductIdValue(),
                    outbox.getSyncType());

            return true;

        } catch (Exception e) {
            // 발행 실패 → PENDING 유지 (스케줄러에서 재처리)
            log.warn(
                    "SQS 발행 실패 (스케줄러에서 재처리): outboxId={}, productId={}, error={}",
                    outbox.getId(),
                    outbox.getCrawledProductIdValue(),
                    e.getMessage());

            return false;
        }
    }
}
