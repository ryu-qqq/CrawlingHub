package com.ryuqq.crawlinghub.adapter.out.sqs.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.sqs.config.SqsProperties;
import com.ryuqq.crawlinghub.adapter.out.sqs.exception.SqsPublishException;
import com.ryuqq.crawlinghub.application.task.dto.messaging.CrawlTaskPayload;
import com.ryuqq.crawlinghub.application.task.port.out.messaging.CrawlTaskMessagePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.event.CrawlTaskRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

/**
 * AWS SQS CrawlTask 메시지 어댑터
 *
 * <p><strong>용도</strong>: CrawlTask 이벤트를 AWS SQS에 발행
 *
 * <p><strong>메시지 발행 방식</strong>:
 *
 * <ul>
 *   <li>{@link #publishFromEvent(CrawlTaskRegisteredEvent)}: 이벤트 리스너에서 호출
 *   <li>{@link #publishFromOutbox(CrawlTaskOutbox)}: 재시도 스케줄러에서 호출
 *   <li>{@link #publish(CrawlTask, String)}: 직접 발행 (레거시 지원)
 * </ul>
 *
 * <p><strong>멱등성 보장</strong>: idempotencyKey를 MessageDeduplicationId로 사용
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskSqsAdapter implements CrawlTaskMessagePort {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskSqsAdapter.class);

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper;

    public CrawlTaskSqsAdapter(
            SqsClient sqsClient, SqsProperties sqsProperties, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.sqsProperties = sqsProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * CrawlTask 메시지 발행 (레거시 지원)
     *
     * @param crawlTask 발행할 CrawlTask
     * @param idempotencyKey 멱등성 키
     */
    @Override
    public void publish(CrawlTask crawlTask, String idempotencyKey) {
        String payload = buildPayloadFromCrawlTask(crawlTask);
        String messageGroupId = buildMessageGroupId(crawlTask.getCrawlSchedulerId().value());

        sendMessage(payload, idempotencyKey, messageGroupId);

        log.info(
                "CrawlTask SQS 메시지 발행 완료: taskId={}, schedulerId={}",
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value());
    }

    /**
     * CrawlTaskRegisteredEvent 기반 메시지 발행
     *
     * <p>트랜잭션 커밋 후 이벤트 리스너에서 호출
     *
     * @param event CrawlTask 등록 이벤트
     */
    @Override
    public void publishFromEvent(CrawlTaskRegisteredEvent event) {
        String payload = event.outboxPayload();
        String idempotencyKey =
                buildIdempotencyKey(event.getCrawlTaskIdValue(), event.getCrawlSchedulerIdValue());
        String messageGroupId = buildMessageGroupId(event.getCrawlSchedulerIdValue());

        sendMessage(payload, idempotencyKey, messageGroupId);

        log.info(
                "CrawlTask SQS 메시지 발행 완료 (이벤트): taskId={}, schedulerId={}, taskType={}",
                event.getCrawlTaskIdValue(),
                event.getCrawlSchedulerIdValue(),
                event.taskType());
    }

    /**
     * Outbox 기반 메시지 발행 (재시도용)
     *
     * <p>PENDING/FAILED 상태의 아웃박스에서 페이로드를 읽어 발행
     *
     * @param outbox 재처리할 아웃박스
     */
    @Override
    public void publishFromOutbox(CrawlTaskOutbox outbox) {
        String payload = outbox.getPayload();
        String idempotencyKey = outbox.getIdempotencyKey();
        String messageGroupId = buildMessageGroupId(outbox.getCrawlTaskId().value());

        sendMessage(payload, idempotencyKey, messageGroupId);

        log.info(
                "CrawlTask SQS 메시지 발행 완료 (Outbox): taskId={}, idempotencyKey={}",
                outbox.getCrawlTaskId().value(),
                idempotencyKey);
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
                        .queueUrl(sqsProperties.getCrawlTaskQueueUrl())
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
     * CrawlTask에서 페이로드 생성
     *
     * @param crawlTask CrawlTask
     * @return JSON 페이로드
     * @throws SqsPublishException 직렬화 실패 시
     */
    private String buildPayloadFromCrawlTask(CrawlTask crawlTask) {
        try {
            CrawlTaskPayload payload = CrawlTaskPayload.from(crawlTask);
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("페이로드 직렬화 실패: taskId={}", crawlTask.getId().value());
            throw new SqsPublishException("페이로드 직렬화 실패", e);
        }
    }

    /**
     * 멱등성 키 생성
     *
     * @param taskId Task ID
     * @param schedulerId Scheduler ID
     * @return 멱등성 키
     */
    private String buildIdempotencyKey(Long taskId, Long schedulerId) {
        return String.format(
                "crawl-task-%d-%d-%d", schedulerId, taskId, System.currentTimeMillis());
    }

    /**
     * 메시지 그룹 ID 생성 (FIFO 큐용)
     *
     * <p>같은 스케줄러의 태스크는 같은 그룹으로 묶어 순서 보장
     *
     * @param schedulerId 스케줄러 ID
     * @return 메시지 그룹 ID
     */
    private String buildMessageGroupId(Long schedulerId) {
        return sqsProperties.getMessageGroupIdPrefix() + schedulerId;
    }

    /**
     * FIFO 큐 여부 확인
     *
     * <p>큐 URL이 .fifo로 끝나면 FIFO 큐
     *
     * @return FIFO 큐이면 true
     */
    private boolean isFifoQueue() {
        String queueUrl = sqsProperties.getCrawlTaskQueueUrl();
        return queueUrl != null && queueUrl.endsWith(".fifo");
    }
}
