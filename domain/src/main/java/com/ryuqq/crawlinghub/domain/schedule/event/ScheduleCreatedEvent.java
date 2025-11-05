package com.ryuqq.crawlinghub.domain.schedule.event;

import java.time.Instant;

/**
 * Schedule Created Event
 *
 * <p>스케줄이 생성되고 Outbox에 저장되었을 때 발생하는 이벤트입니다.</p>
 * <p>이 이벤트는 트랜잭션 커밋 후 발행되며, EventListener에서 비동기로 Outbox Processor를 호출합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ScheduleCreatedEvent(
    Long scheduleId,
    Long sellerId,
    String cronExpression,
    String outboxIdemKey,
    Instant occurredAt
) implements ScheduleEvent {

    /**
     * 정적 팩토리 메서드
     *
     * @param scheduleId 스케줄 ID
     * @param sellerId 셀러 ID
     * @param cronExpression Cron 표현식
     * @param outboxIdemKey Outbox Idempotency Key
     * @return ScheduleCreatedEvent
     */
    public static ScheduleCreatedEvent of(
        Long scheduleId,
        Long sellerId,
        String cronExpression,
        String outboxIdemKey
    ) {
        return new ScheduleCreatedEvent(
            scheduleId,
            sellerId,
            cronExpression,
            outboxIdemKey,
            Instant.now()
        );
    }
}

