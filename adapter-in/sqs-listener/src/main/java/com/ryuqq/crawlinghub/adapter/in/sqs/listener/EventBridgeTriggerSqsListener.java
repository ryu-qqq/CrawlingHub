package com.ryuqq.crawlinghub.adapter.in.sqs.listener;

import com.ryuqq.crawlinghub.application.common.component.lock.DistributedLockExecutor;
import com.ryuqq.crawlinghub.application.common.component.lock.LockType;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.messaging.EventBridgeTriggerPayload;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * EventBridge 트리거 SQS 리스너
 *
 * <p><strong>용도</strong>: EventBridge에서 발행한 스케줄러 트리거 메시지 수신
 *
 * <p><strong>트리거 흐름</strong>:
 *
 * <pre>
 * EventBridge Rule (cron 스케줄)
 *     ↓
 * SQS (eventbridge-trigger-queue)
 *     ↓
 * EventBridgeTriggerSqsListener (이 클래스)
 *     ↓
 * CrawlTask 생성 → CrawlTask SQS 발행
 * </pre>
 *
 * <p><strong>분산 락</strong>:
 *
 * <ul>
 *   <li>락 획득 성공 → UseCase 호출 → 메시지 ACK
 *   <li>락 획득 실패 → 메시지 ACK (다른 워커가 처리 중이므로 skip)
 * </ul>
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
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.event-bridge-trigger-listener-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class EventBridgeTriggerSqsListener {

    private static final Logger log = LoggerFactory.getLogger(EventBridgeTriggerSqsListener.class);

    private final DistributedLockExecutor lockExecutor;
    private final TriggerCrawlTaskUseCase triggerCrawlTaskUseCase;

    public EventBridgeTriggerSqsListener(
            DistributedLockExecutor lockExecutor, TriggerCrawlTaskUseCase triggerCrawlTaskUseCase) {
        this.lockExecutor = lockExecutor;
        this.triggerCrawlTaskUseCase = triggerCrawlTaskUseCase;
    }

    /**
     * EventBridge 트리거 메시지 수신 및 처리
     *
     * <p>Spring Cloud AWS SQS의 자동 메시지 역직렬화 사용
     *
     * <p>MANUAL acknowledgement 모드로 명시적 ACK 처리
     *
     * @param payload EventBridge 트리거 페이로드
     * @param acknowledgement 메시지 ACK 핸들러
     */
    @SqsListener(
            value = "${aws.sqs.listener.event-bridge-trigger-queue-url}",
            acknowledgementMode = "MANUAL")
    public void handleMessage(
            @Payload EventBridgeTriggerPayload payload, Acknowledgement acknowledgement) {
        Long schedulerId = payload.schedulerId();

        log.debug(
                "EventBridge 트리거 메시지 수신: schedulerId={}, sellerId={}, schedulerName={},"
                        + " triggerTime={}",
                schedulerId,
                payload.sellerId(),
                payload.schedulerName(),
                payload.triggerTime());

        try {
            // 분산 락 획득 시도 (schedulerId 기준)
            boolean executed =
                    lockExecutor.tryExecuteWithLock(
                            LockType.CRAWL_TRIGGER, schedulerId, () -> handleTrigger(payload));

            // 락 획득 성공/실패 모두 ACK
            // - 성공: 처리 완료
            // - 실패: 다른 워커가 처리 중이므로 skip
            acknowledgement.acknowledge();

            if (executed) {
                log.info(
                        "EventBridge 트리거 처리 완료: schedulerId={}, schedulerName={}",
                        schedulerId,
                        payload.schedulerName());
            } else {
                log.info("EventBridge 트리거 처리 skip (다른 워커 처리 중): schedulerId={}", schedulerId);
            }

        } catch (Exception e) {
            log.error(
                    "EventBridge 트리거 처리 실패: schedulerId={}, error={}",
                    schedulerId,
                    e.getMessage(),
                    e);
            // 처리 실패 시 ACK 하지 않음 → visibility timeout 후 재시도
            throw e;
        }
    }

    /**
     * 트리거 처리
     *
     * <p>TriggerCrawlTaskUseCase를 호출하여 CrawlTask 생성 및 SQS 발행
     *
     * @param payload 트리거 정보
     */
    private void handleTrigger(EventBridgeTriggerPayload payload) {
        log.debug(
                "스케줄러 트리거 처리 시작: schedulerId={}, sellerId={}, schedulerName={}",
                payload.schedulerId(),
                payload.sellerId(),
                payload.schedulerName());

        TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(payload.schedulerId());
        triggerCrawlTaskUseCase.execute(command);
    }
}
