package com.ryuqq.crawlinghub.adapter.out.sqs.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.sqs.config.SqsClientProperties;
import com.ryuqq.crawlinghub.adapter.out.sqs.exception.SqsPublishException;
import com.ryuqq.crawlinghub.application.task.dto.messaging.CrawlTaskPayload;
import com.ryuqq.crawlinghub.application.task.port.out.client.CrawlTaskMessageClient;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Component
@ConditionalOnProperty(prefix = "sqs.queues", name = "crawl-task")
public class CrawlTaskSqsAdapter implements CrawlTaskMessageClient {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskSqsAdapter.class);

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final String messageGroupIdPrefix;
    private final ObjectMapper objectMapper;

    public CrawlTaskSqsAdapter(
            SqsClient sqsClient, SqsClientProperties properties, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.queueUrl = properties.getQueues().getCrawlTask();
        this.messageGroupIdPrefix = properties.getMessageGroupIdPrefix();
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(CrawlTask crawlTask, String idempotencyKey) {
        String payload = buildPayloadFromCrawlTask(crawlTask);
        String messageGroupId = buildMessageGroupId(crawlTask.getCrawlSchedulerIdValue());

        sendMessage(payload, idempotencyKey, messageGroupId);

        log.info(
                "CrawlTask SQS 메시지 발행 완료: taskId={}, schedulerId={}",
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue());
    }

    @Override
    public void publishFromOutbox(CrawlTaskOutbox outbox) {
        String payload = outbox.getPayload();
        String idempotencyKey = outbox.getIdempotencyKey();
        String messageGroupId = buildMessageGroupId(outbox.getCrawlTaskIdValue());

        sendMessage(payload, idempotencyKey, messageGroupId);

        log.info(
                "CrawlTask SQS 메시지 발행 완료 (Outbox): taskId={}, idempotencyKey={}",
                outbox.getCrawlTaskIdValue(),
                idempotencyKey);
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

    private String buildPayloadFromCrawlTask(CrawlTask crawlTask) {
        try {
            CrawlTaskPayload payload = CrawlTaskPayload.from(crawlTask);
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("페이로드 직렬화 실패: taskId={}", crawlTask.getIdValue());
            throw new SqsPublishException("페이로드 직렬화 실패", e);
        }
    }

    private String buildMessageGroupId(Long schedulerId) {
        return messageGroupIdPrefix + schedulerId;
    }

    private boolean isFifoQueue() {
        return queueUrl != null && queueUrl.endsWith(".fifo");
    }
}
