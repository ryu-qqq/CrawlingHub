package com.ryuqq.crawlinghub.adapter.in.sqs.listener;

import com.ryuqq.crawlinghub.application.task.dto.messaging.EventBridgeTriggerPayload;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * EventBridge 트리거 DLQ 리스너
 *
 * <p><strong>용도</strong>: EventBridge 트리거 처리 실패 메시지 수신 및 로깅
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <pre>
 * eventbridge-trigger-queue (재시도 실패)
 *     ↓
 * eventbridge-trigger-dlq (maxReceiveCount 초과)
 *     ↓
 * EventBridgeTriggerDlqListener (이 클래스)
 *     ↓
 * 실패 로깅 (알림/모니터링용)
 * </pre>
 *
 * <p><strong>메시지 페이로드</strong>:
 *
 * <pre>{@code
 * {
 *   "schedulerId": 123,
 *   "sellerId": 456,
 *   "schedulerName": "daily-product-sync",
 *   "triggerTime": "2024-01-15T10:00:00Z"
 * }
 * }</pre>
 *
 * <p><strong>참고</strong>: EventBridge 트리거는 직접 SQS로 전송되므로 별도의 Outbox가 없습니다. 실패한 트리거는 로깅을 통해 모니터링/알림
 * 시스템에서 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.event-bridge-trigger-dlq-listener-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class EventBridgeTriggerDlqListener {

    private static final Logger log = LoggerFactory.getLogger(EventBridgeTriggerDlqListener.class);

    /**
     * DLQ 메시지 수신 및 처리
     *
     * <p>Spring Cloud AWS SQS의 자동 메시지 역직렬화 사용
     *
     * <p>MANUAL acknowledgement 모드로 명시적 ACK 처리
     *
     * <p>EventBridge 트리거는 Outbox 패턴을 사용하지 않으므로, 실패 로깅 후 ACK 처리
     *
     * @param payload EventBridge 트리거 페이로드
     * @param acknowledgement 메시지 ACK 핸들러
     */
    @SqsListener(
            value = "${aws.sqs.listener.event-bridge-trigger-dlq-url}",
            acknowledgementMode = "MANUAL")
    public void handleMessage(
            @Payload EventBridgeTriggerPayload payload, Acknowledgement acknowledgement) {
        Long schedulerId = payload.schedulerId();

        log.warn(
                "EventBridge 트리거 DLQ 메시지 수신 (처리 실패): schedulerId={}, sellerId={}, "
                        + "schedulerName={}, triggerTime={}",
                schedulerId,
                payload.sellerId(),
                payload.schedulerName(),
                payload.triggerTime());

        // 실패 로깅 완료 후 ACK
        // 추가 조치가 필요한 경우 모니터링/알림 시스템에서 로그를 수집하여 처리
        acknowledgement.acknowledge();

        log.info(
                "EventBridge 트리거 DLQ 처리 완료 (로깅): schedulerId={}, schedulerName={}",
                schedulerId,
                payload.schedulerName());
    }
}
