package com.ryuqq.crawlinghub.application.sync.manager.messaging;

import com.ryuqq.crawlinghub.application.sync.port.out.messaging.ProductSyncMessagePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ProductSync 메시지 관리자
 *
 * <p><strong>책임</strong>: ProductSync SQS 메시지 발행 관리
 *
 * <p><strong>발행 방식</strong>:
 *
 * <ul>
 *   <li>{@link #publishFromEvent(ExternalSyncRequestedEvent)}: 이벤트 기반 발행 (트랜잭션 커밋 후)
 *   <li>{@link #publishFromOutbox(CrawledProductSyncOutbox)}: Outbox 기반 발행 (재시도용)
 *   <li>{@link #publish(CrawledProductSyncOutbox)}: 직접 발행
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductSyncMessageManager {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncMessageManager.class);

    private final ProductSyncMessagePort productSyncMessagePort;

    public ProductSyncMessageManager(ProductSyncMessagePort productSyncMessagePort) {
        this.productSyncMessagePort = productSyncMessagePort;
    }

    /**
     * ExternalSyncRequestedEvent 기반 메시지 발행
     *
     * <p>트랜잭션 커밋 후 이벤트 리스너에서 호출
     *
     * @param event 외부 동기화 요청 이벤트
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publishFromEvent(ExternalSyncRequestedEvent event) {
        log.debug(
                "ProductSync 메시지 발행 시작 (이벤트): crawledProductId={}, sellerId={}, itemNo={}",
                event.crawledProductId().value(),
                event.sellerId().value(),
                event.itemNo());

        productSyncMessagePort.publishFromEvent(event);

        log.info(
                "ProductSync 메시지 발행 완료 (이벤트): crawledProductId={}, syncType={}",
                event.crawledProductId().value(),
                event.syncType());
    }

    /**
     * Outbox 기반 메시지 발행 (재시도용)
     *
     * <p>PENDING/FAILED 상태의 Outbox에서 페이로드를 읽어 발행
     *
     * @param outbox 재처리할 Outbox
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publishFromOutbox(CrawledProductSyncOutbox outbox) {
        log.debug(
                "ProductSync 메시지 발행 시작 (Outbox): outboxId={}, crawledProductId={},"
                        + " idempotencyKey={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getIdempotencyKey());

        productSyncMessagePort.publishFromOutbox(outbox);

        log.info(
                "ProductSync 메시지 발행 완료 (Outbox): outboxId={}, crawledProductId={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue());
    }

    /**
     * CrawledProductSyncOutbox 직접 발행
     *
     * @param outbox 발행할 CrawledProductSyncOutbox
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publish(CrawledProductSyncOutbox outbox) {
        log.debug(
                "ProductSync 메시지 발행 시작: outboxId={}, crawledProductId={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue());

        productSyncMessagePort.publish(outbox);

        log.info(
                "ProductSync 메시지 발행 완료: outboxId={}, crawledProductId={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue());
    }
}
