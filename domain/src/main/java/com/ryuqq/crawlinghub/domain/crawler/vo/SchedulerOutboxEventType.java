package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * SchedulerOutbox 이벤트 타입 Enum
 *
 * <p>SchedulerOutbox에서 발행하는 스케줄러 관련 이벤트 종류를 정의합니다.</p>
 *
 * <p><strong>이벤트 타입:</strong></p>
 * <ul>
 *   <li>{@link #SCHEDULE_REGISTERED} - 스케줄 등록 이벤트 (EventBridge에 Rule 생성)</li>
 *   <li>{@link #SCHEDULE_UPDATED} - 스케줄 업데이트 이벤트 (EventBridge Rule 수정)</li>
 *   <li>{@link #SCHEDULE_DEACTIVATED} - 스케줄 비활성화 이벤트 (EventBridge Rule 비활성화)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public enum SchedulerOutboxEventType {

    /**
     * 스케줄 등록 이벤트
     *
     * <p>EventBridge에 새로운 Rule을 생성합니다.</p>
     */
    SCHEDULE_REGISTERED,

    /**
     * 스케줄 업데이트 이벤트
     *
     * <p>EventBridge의 기존 Rule을 수정합니다.</p>
     */
    SCHEDULE_UPDATED,

    /**
     * 스케줄 비활성화 이벤트
     *
     * <p>EventBridge의 Rule을 비활성화합니다.</p>
     */
    SCHEDULE_DEACTIVATED;

    /**
     * String 값으로부터 SchedulerOutboxEventType 생성
     *
     * @param value 문자열 값
     * @return SchedulerOutboxEventType enum
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutboxEventType of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("SchedulerOutboxEventType cannot be null");
        }
        return valueOf(value.toUpperCase());
    }
}
