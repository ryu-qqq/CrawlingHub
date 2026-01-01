package com.ryuqq.crawlinghub.application.image.manager.messaging;

import com.ryuqq.crawlinghub.application.image.port.out.messaging.ProductImageMessagePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ProductImage 메시지 관리자
 *
 * <p><strong>책임</strong>: ProductImage SQS 메시지 발행 관리
 *
 * <p><strong>발행 방식</strong>:
 *
 * <ul>
 *   <li>{@link #publishFromEvent(ImageUploadRequestedEvent)}: 이벤트 기반 발행 (트랜잭션 커밋 후)
 *   <li>{@link #publishFromOutbox(ProductImageOutbox)}: Outbox 기반 발행 (재시도용)
 *   <li>{@link #publish(ProductImageOutbox)}: 직접 발행
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductImageMessageManager {

    private static final Logger log = LoggerFactory.getLogger(ProductImageMessageManager.class);

    private final ProductImageMessagePort productImageMessagePort;

    public ProductImageMessageManager(ProductImageMessagePort productImageMessagePort) {
        this.productImageMessagePort = productImageMessagePort;
    }

    /**
     * ImageUploadRequestedEvent 기반 메시지 발행
     *
     * <p>트랜잭션 커밋 후 이벤트 리스너에서 호출
     *
     * @param event 이미지 업로드 요청 이벤트
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publishFromEvent(ImageUploadRequestedEvent event) {
        log.debug(
                "ProductImage 메시지 발행 시작 (이벤트): crawledProductId={}, targetCount={}",
                event.crawledProductId().value(),
                event.targets().size());

        productImageMessagePort.publishFromEvent(event);

        log.info(
                "ProductImage 메시지 발행 완료 (이벤트): crawledProductId={}, targetCount={}",
                event.crawledProductId().value(),
                event.targets().size());
    }

    /**
     * Outbox 기반 메시지 발행 (재시도용)
     *
     * <p>PENDING/FAILED 상태의 Outbox에서 페이로드를 읽어 발행
     *
     * @param outbox 재처리할 Outbox
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publishFromOutbox(ProductImageOutbox outbox) {
        log.debug(
                "ProductImage 메시지 발행 시작 (Outbox): outboxId={}, imageId={}, idempotencyKey={}",
                outbox.getId(),
                outbox.getCrawledProductImageId(),
                outbox.getIdempotencyKey());

        productImageMessagePort.publishFromOutbox(outbox);

        log.info(
                "ProductImage 메시지 발행 완료 (Outbox): outboxId={}, imageId={}",
                outbox.getId(),
                outbox.getCrawledProductImageId());
    }

    /**
     * ProductImageOutbox 직접 발행
     *
     * @param outbox 발행할 ProductImageOutbox
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publish(ProductImageOutbox outbox) {
        log.debug(
                "ProductImage 메시지 발행 시작: outboxId={}, imageId={}",
                outbox.getId(),
                outbox.getCrawledProductImageId());

        productImageMessagePort.publish(outbox);

        log.info(
                "ProductImage 메시지 발행 완료: outboxId={}, imageId={}",
                outbox.getId(),
                outbox.getCrawledProductImageId());
    }
}
