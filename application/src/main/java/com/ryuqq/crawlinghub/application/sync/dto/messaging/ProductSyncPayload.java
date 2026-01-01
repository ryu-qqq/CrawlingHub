package com.ryuqq.crawlinghub.application.sync.dto.messaging;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;

/**
 * ProductSync SQS 메시지 페이로드
 *
 * <p><strong>용도</strong>: SQS를 통해 전송되는 외부 서버 동기화 메시지의 페이로드 구조
 *
 * <p><strong>사용처</strong>:
 *
 * <ul>
 *   <li>adapter-out/aws-sqs: 메시지 발행 시 직렬화
 *   <li>adapter-in/sqs-listener: 메시지 수신 시 역직렬화
 * </ul>
 *
 * @param outboxId Outbox ID
 * @param crawledProductId CrawledProduct ID
 * @param sellerId Seller ID
 * @param itemNo Item No
 * @param syncType 동기화 유형 (CREATE, UPDATE)
 * @param externalProductId 외부 상품 ID (UPDATE 시)
 * @param idempotencyKey 멱등성 키
 * @author development-team
 * @since 1.0.0
 */
public record ProductSyncPayload(
        Long outboxId,
        Long crawledProductId,
        Long sellerId,
        Long itemNo,
        String syncType,
        Long externalProductId,
        String idempotencyKey) {

    /**
     * CrawledProductSyncOutbox로부터 페이로드 생성
     *
     * @param outbox CrawledProductSyncOutbox
     * @return ProductSyncPayload
     */
    public static ProductSyncPayload from(CrawledProductSyncOutbox outbox) {
        return new ProductSyncPayload(
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getSellerIdValue(),
                outbox.getItemNo(),
                outbox.getSyncType().name(),
                outbox.getExternalProductId(),
                outbox.getIdempotencyKey());
    }
}
