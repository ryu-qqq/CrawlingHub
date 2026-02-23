package com.ryuqq.crawlinghub.adapter.in.sqs.scheduler;

import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.messaging.EventBridgeTriggerPayload;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import io.awspring.cloud.sqs.annotation.SqsListener;
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
 * <p><strong>에러 분류</strong>:
 *
 * <ul>
 *   <li>영구적 오류 (DomainException 계열): 비즈니스 검증 실패 → 로그 후 삼킴 (ACK, 재시도 무의미)
 *   <li>일시적 오류 (그 외): 인프라 오류 → throw (SQS 재시도)
 * </ul>
 *
 * <p><strong>멱등성</strong>: Application Layer에서 보장
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

    private final EventBridgeTriggerListenerMapper mapper;
    private final TriggerCrawlTaskUseCase triggerCrawlTaskUseCase;

    public EventBridgeTriggerSqsListener(
            EventBridgeTriggerListenerMapper mapper,
            TriggerCrawlTaskUseCase triggerCrawlTaskUseCase) {
        this.mapper = mapper;
        this.triggerCrawlTaskUseCase = triggerCrawlTaskUseCase;
    }

    /**
     * EventBridge 트리거 메시지 수신 및 처리
     *
     * <p>AUTO ACK 모드: 정상 반환 시 자동 ACK, 예외 발생 시 SQS 재시도
     *
     * @param payload EventBridge 트리거 페이로드
     */
    @SqsListener("${aws.sqs.listener.event-bridge-trigger-queue-url}")
    public void handleMessage(@Payload EventBridgeTriggerPayload payload) {
        Long schedulerId = payload.schedulerId();

        log.debug(
                "EventBridge 트리거 메시지 수신: schedulerId={}, sellerId={}, schedulerName={},"
                        + " triggerTime={}",
                schedulerId,
                payload.sellerId(),
                payload.schedulerName(),
                payload.triggerTime());

        try {
            TriggerCrawlTaskCommand command = mapper.toCommand(payload);
            triggerCrawlTaskUseCase.execute(command);
            log.info(
                    "EventBridge 트리거 처리 완료: schedulerId={}, schedulerName={}",
                    schedulerId,
                    payload.schedulerName());
        } catch (Exception e) {
            if (isPermanent(e)) {
                log.warn(
                        "EventBridge 트리거 영구적 오류 (skip): schedulerId={}, error={}",
                        schedulerId,
                        e.getMessage());
                return; // ACK (재시도 무의미)
            }
            log.warn("EventBridge 트리거 일시적 오류, SQS 재시도 위임: schedulerId={}", schedulerId);
            throw e;
        }
    }

    /**
     * 영구적 오류 여부 판별
     *
     * <p>DomainException 계열 (비즈니스 검증 실패) → 재시도해도 결과 동일
     *
     * @param e 발생한 예외
     * @return 영구적 오류이면 true
     */
    private boolean isPermanent(Exception e) {
        return e instanceof DomainException;
    }
}
