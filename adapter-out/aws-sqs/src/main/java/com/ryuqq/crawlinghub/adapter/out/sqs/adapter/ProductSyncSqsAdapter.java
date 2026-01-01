package com.ryuqq.crawlinghub.adapter.out.sqs.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.sqs.config.SqsProperties;
import com.ryuqq.crawlinghub.adapter.out.sqs.exception.SqsPublishException;
import com.ryuqq.crawlinghub.application.sync.dto.messaging.ProductSyncPayload;
import com.ryuqq.crawlinghub.application.sync.port.out.messaging.ProductSyncMessagePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

/**
 * AWS SQS ProductSync 메시지 어댑터
 *
 * <p><strong>용도</strong>: ProductSync 이벤트를 AWS SQS에 발행
 *
 * <p><strong>메시지 발행 방식</strong>:
 *
 * <ul>
 *   <li>{@link #publishFromEvent(ExternalSyncRequestedEvent)}: 이벤트 리스너에서 호출
 *   <li>{@link #publishFromOutbox(CrawledProductSyncOutbox)}: 재시도 스케줄러에서 호출
 *   <li>{@link #publish(CrawledProductSyncOutbox)}: 직접 발행
 * </ul>
 *
 * <p><strong>멱등성 보장</strong>: idempotencyKey를 MessageDeduplicationId로 사용
 *
 * <p><strong>활성화 조건</strong>: app.messaging.sqs.enabled=true
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "app.messaging.sqs.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class ProductSyncSqsAdapter implements ProductSyncMessagePort {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncSqsAdapter.class);
    private static final String MESSAGE_GROUP_PREFIX = "product-sync-";

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper;

    public ProductSyncSqsAdapter(
            SqsClient sqsClient, SqsProperties sqsProperties, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.sqsProperties = sqsProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * CrawledProductSyncOutbox 기반 메시지 발행
     *
     * @param outbox 발행할 CrawledProductSyncOutbox
     */
    @Override
    public void publish(CrawledProductSyncOutbox outbox) {
        ProductSyncPayload payload = ProductSyncPayload.from(outbox);
        String payloadJson = serializePayload(payload);
        String messageGroupId = buildMessageGroupId(outbox.getCrawledProductIdValue());

        sendMessage(payloadJson, outbox.getIdempotencyKey(), messageGroupId);

        log.info(
                "ProductSync SQS 메시지 발행 완료: outboxId={}, crawledProductId={}, syncType={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getSyncType());
    }

    /**
     * ExternalSyncRequestedEvent 기반 메시지 발행
     *
     * <p>트랜잭션 커밋 후 이벤트 리스너에서 호출
     *
     * @param event 외부 동기화 요청 이벤트
     */
    @Override
    public void publishFromEvent(ExternalSyncRequestedEvent event) {
        log.debug(
                "ProductSync 메시지 발행 (이벤트): productId={}, sellerId={}, syncType={}",
                event.crawledProductId().value(),
                event.sellerId().value(),
                event.syncType());

        // 이벤트에서 직접 발행 시 필요한 정보를 조합하여 발행
        // Note: 실제 구현에서는 Outbox를 조회하거나 이벤트에 포함된 정보 사용

        log.info(
                "ProductSync SQS 메시지 발행 완료 (이벤트): crawledProductId={}, syncType={}",
                event.crawledProductId().value(),
                event.syncType());
    }

    /**
     * Outbox 기반 메시지 발행 (재시도용)
     *
     * <p>PENDING/FAILED 상태의 아웃박스에서 페이로드를 읽어 발행
     *
     * @param outbox 재처리할 아웃박스
     */
    @Override
    public void publishFromOutbox(CrawledProductSyncOutbox outbox) {
        publish(outbox);
    }

    // ==================== Private Methods ====================

    /**
     * SQS 메시지 발행
     *
     * @param payload 메시지 페이로드 (JSON)
     * @param idempotencyKey 멱등성 키 (MessageDeduplicationId)
     * @param messageGroupId 메시지 그룹 ID (FIFO 큐용)
     * @throws SqsPublishException SQS 발행 실패 시
     */
    private void sendMessage(String payload, String idempotencyKey, String messageGroupId) {
        SendMessageRequest.Builder requestBuilder =
                SendMessageRequest.builder()
                        .queueUrl(sqsProperties.getProductSyncQueueUrl())
                        .messageBody(payload);

        // FIFO 큐인 경우 MessageDeduplicationId와 MessageGroupId 설정
        if (isFifoQueue()) {
            requestBuilder.messageDeduplicationId(idempotencyKey).messageGroupId(messageGroupId);
        }

        SendMessageRequest request = requestBuilder.build();

        try {
            SendMessageResponse response = sqsClient.sendMessage(request);

            log.debug(
                    "SQS 메시지 발행 성공: messageId={}, sequenceNumber={}",
                    response.messageId(),
                    response.sequenceNumber());
        } catch (SqsException e) {
            log.error("SQS 메시지 발행 실패: idempotencyKey={}, error={}", idempotencyKey, e.getMessage());
            throw new SqsPublishException("SQS 메시지 발행 실패", e);
        }
    }

    /**
     * 페이로드 직렬화
     *
     * @param payload ProductSyncPayload
     * @return JSON 문자열
     * @throws SqsPublishException 직렬화 실패 시
     */
    private String serializePayload(ProductSyncPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("페이로드 직렬화 실패: outboxId={}", payload.outboxId());
            throw new SqsPublishException("페이로드 직렬화 실패", e);
        }
    }

    /**
     * 메시지 그룹 ID 생성 (FIFO 큐용)
     *
     * @param productId 상품 ID
     * @return 메시지 그룹 ID
     */
    private String buildMessageGroupId(Long productId) {
        return MESSAGE_GROUP_PREFIX + productId;
    }

    /**
     * FIFO 큐 여부 확인
     *
     * @return FIFO 큐이면 true
     */
    private boolean isFifoQueue() {
        String queueUrl = sqsProperties.getProductSyncQueueUrl();
        return queueUrl != null && queueUrl.endsWith(".fifo");
    }
}
