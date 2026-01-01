package com.ryuqq.crawlinghub.application.image.dto.messaging;

import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;

/**
 * ProductImage SQS 메시지 페이로드
 *
 * <p><strong>용도</strong>: SQS를 통해 전송되는 이미지 업로드 메시지의 페이로드 구조
 *
 * <p><strong>사용처</strong>:
 *
 * <ul>
 *   <li>adapter-out/aws-sqs: 메시지 발행 시 직렬화
 *   <li>adapter-in/sqs-listener: 메시지 수신 시 역직렬화
 * </ul>
 *
 * @param outboxId Outbox ID
 * @param crawledProductImageId CrawledProductImage ID
 * @param idempotencyKey 멱등성 키
 * @author development-team
 * @since 1.0.0
 */
public record ProductImagePayload(
        Long outboxId, Long crawledProductImageId, String idempotencyKey) {

    /**
     * ProductImageOutbox로부터 페이로드 생성
     *
     * @param outbox ProductImageOutbox
     * @return ProductImagePayload
     */
    public static ProductImagePayload from(ProductImageOutbox outbox) {
        return new ProductImagePayload(
                outbox.getId(), outbox.getCrawledProductImageId(), outbox.getIdempotencyKey());
    }
}
