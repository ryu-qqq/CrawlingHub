package com.ryuqq.crawlinghub.domain.schedule.outbox;

/**
 * EventBridge 이벤트 타입
 *
 * <p>EventBridge Rule 생성/수정/삭제 이벤트를 정의합니다.
 *
 * @author windsurf
 * @since 1.0.0
 */
public enum EventType {
    /** EventBridge Rule 등록 */
    EVENTBRIDGE_REGISTER("EventBridge 등록"),

    /** EventBridge Rule 수정 */
    EVENTBRIDGE_UPDATE("EventBridge 수정"),

    /** EventBridge Rule 삭제 (향후 확장) */
    EVENTBRIDGE_DELETE("EventBridge 삭제");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 문자열로부터 EventType 생성
     *
     * @param eventTypeStr 이벤트 타입 문자열
     * @return EventType
     * @throws IllegalArgumentException 유효하지 않은 이벤트 타입인 경우
     */
    public static EventType fromString(String eventTypeStr) {
        if (eventTypeStr == null || eventTypeStr.isBlank()) {
            throw new IllegalArgumentException("EventType은 필수입니다");
        }

        try {
            return EventType.valueOf(eventTypeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 EventType입니다: " + eventTypeStr);
        }
    }
}
