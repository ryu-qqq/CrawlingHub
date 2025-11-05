package com.ryuqq.crawlinghub.domain.schedule.event;

import com.ryuqq.crawlinghub.domain.common.DomainEvent;

import java.time.Instant;

/**
 * Schedule Event - Sealed Interface
 *
 * <p>Schedule Bounded Context의 모든 도메인 이벤트를 묶는 Sealed 인터페이스입니다.</p>
 *
 * <p><strong>이벤트 계층:</strong></p>
 * <ul>
 *   <li>ScheduleCreatedEvent - 스케줄 생성 이벤트</li>
 *   <li>ScheduleUpdatedEvent - 스케줄 수정 이벤트</li>
 * </ul>
 *
 * <p><strong>Sealed Classes 장점:</strong></p>
 * <ul>
 *   <li>✅ 허용된 이벤트만 정의 가능 (컴파일 타임 검증)</li>
 *   <li>✅ Switch Expression에서 Exhaustive Checking (모든 케이스 처리 강제)</li>
 *   <li>✅ 타입 안전성 향상</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public sealed interface ScheduleEvent extends DomainEvent
    permits ScheduleCreatedEvent, ScheduleUpdatedEvent {

    /**
     * 스케줄 ID 반환
     *
     * @return 스케줄 ID
     */
    Long scheduleId();

    /**
     * 셀러 ID 반환
     *
     * @return 셀러 ID
     */
    Long sellerId();

    /**
     * 이벤트 발생 시각
     *
     * @return 발생 시각
     */
    Instant occurredAt();
}

