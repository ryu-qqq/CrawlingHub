package com.ryuqq.crawlinghub.adapter.out.sqs.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.sqs.config.SqsClientProperties;
import com.ryuqq.crawlinghub.adapter.out.sqs.exception.SqsPublishException;
import com.ryuqq.crawlinghub.application.product.dto.messaging.ProductSyncPayload;
import com.ryuqq.crawlinghub.application.product.port.out.client.CrawledProductSyncMessageClient;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Component
@ConditionalOnProperty(prefix = "sqs.queues", name = "product-sync")
public class CrawledProductSyncSqsAdapter implements CrawledProductSyncMessageClient {

    private static final Logger log = LoggerFactory.getLogger(CrawledProductSyncSqsAdapter.class);
    private static final String MESSAGE_GROUP_PREFIX = "product-sync-";

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final ObjectMapper objectMapper;

    public CrawledProductSyncSqsAdapter(
            SqsClient sqsClient, SqsClientProperties properties, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.queueUrl = properties.getQueues().getProductSync();
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(CrawledProductSyncOutbox outbox) {
        String payload = buildPayload(outbox);
        String messageGroupId = buildMessageGroupId(outbox.getCrawledProductIdValue());

        sendMessage(payload, outbox.getIdempotencyKey(), messageGroupId);

        log.info(
                "CrawledProductSync SQS 메시지 발행 완료: outboxId={}, crawledProductId={}, syncType={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getSyncType());
    }

    private void sendMessage(String payload, String idempotencyKey, String messageGroupId) {
        SendMessageRequest.Builder requestBuilder =
                SendMessageRequest.builder().queueUrl(queueUrl).messageBody(payload);

        if (isFifoQueue()) {
            requestBuilder.messageDeduplicationId(idempotencyKey).messageGroupId(messageGroupId);
        }

        try {
            SendMessageResponse response = sqsClient.sendMessage(requestBuilder.build());

            log.debug(
                    "SQS 메시지 발행 성공: messageId={}, sequenceNumber={}",
                    response.messageId(),
                    response.sequenceNumber());
        } catch (SqsException e) {
            log.error("SQS 메시지 발행 실패: idempotencyKey={}, error={}", idempotencyKey, e.getMessage());
            throw new SqsPublishException("SQS 메시지 발행 실패", e);
        }
    }

    private String buildPayload(CrawledProductSyncOutbox outbox) {
        try {
            ProductSyncPayload payload = ProductSyncPayload.from(outbox);
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("페이로드 직렬화 실패: outboxId={}", outbox.getId());
            throw new SqsPublishException("페이로드 직렬화 실패", e);
        }
    }

    private String buildMessageGroupId(Long productId) {
        return MESSAGE_GROUP_PREFIX + productId;
    }

    private boolean isFifoQueue() {
        return queueUrl != null && queueUrl.endsWith(".fifo");
    }
}
