package com.ryuqq.crawlinghub.adapter.out.aws.sqs;

import com.ryuqq.crawlinghub.application.task.port.out.PublishTaskMessagePort;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

/**
 * Task 메시지 SQS 발행 Adapter (Outbound Adapter)
 *
 * <p>PublishTaskMessagePort 구현체
 *
 * <p>Hexagonal Architecture 위치:
 * - Outbound Adapter (Application Layer에서 외부 시스템으로 전달)
 * - AWS SQS와의 통신 담당
 *
 * <p>구현 사항:
 * - AWS SDK v2 사용 (software.amazon.awssdk:sqs)
 * - SqsClient를 통한 sendMessage() 구현
 * - 환경 변수로 Queue URL 관리 (application.yml: aws.sqs.queue-url)
 * - SqsException 예외 처리
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class TaskMessageSqsAdapter implements PublishTaskMessagePort {

    private static final Logger log = LoggerFactory.getLogger(TaskMessageSqsAdapter.class);

    private final SqsClient sqsClient;
    private final String queueUrl;

    public TaskMessageSqsAdapter(
        SqsClient sqsClient,
        @Value("${aws.sqs.queue-url}") String queueUrl
    ) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    /**
     * Task 메시지를 SQS로 발행
     *
     * <p>AWS SDK v2를 사용한 실제 SQS 발행 구현
     *
     * @param taskId Task ID
     * @param taskType Task 타입
     * @throws RuntimeException SQS 발행 실패 시
     */
    @Override
    public void publish(TaskId taskId, TaskType taskType) {
        try {
            log.info("Task 메시지 SQS 발행 시작. taskId={}, taskType={}", taskId.value(), taskType);

            // 1. JSON 메시지 Body 생성
            String messageBody = createMessageBody(taskId, taskType);

            // 2. SendMessageRequest 생성
            SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();

            // 3. SQS 발행
            SendMessageResponse response = sqsClient.sendMessage(request);

            log.info("Task 메시지 SQS 발행 완료. taskId={}, taskType={}, messageId={}",
                taskId.value(), taskType, response.messageId());

        } catch (SqsException e) {
            log.error("Task 메시지 SQS 발행 실패 (SqsException). taskId={}, taskType={}, statusCode={}, error={}",
                taskId.value(), taskType, e.statusCode(), e.getMessage(), e);
            throw new RuntimeException("SQS 발행 실패: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Task 메시지 SQS 발행 실패 (Exception). taskId={}, taskType={}, error={}",
                taskId.value(), taskType, e.getMessage(), e);
            throw new RuntimeException("SQS 발행 실패: " + e.getMessage(), e);
        }
    }

    /**
     * SQS 메시지 Body 생성
     *
     * <p>JSON 형식:
     * <pre>
     * {
     *   "taskId": 123,
     *   "taskType": "MINI_SHOP"
     * }
     * </pre>
     *
     * @param taskId Task ID
     * @param taskType Task 타입
     * @return JSON 문자열
     */
    private String createMessageBody(TaskId taskId, TaskType taskType) {
        return String.format(
            "{\"taskId\":%d,\"taskType\":\"%s\"}",
            taskId.value(),
            taskType.name()
        );
    }
}
