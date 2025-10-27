package com.ryuqq.crawlinghub.application.mustit.seller.service;

/**
 * 스케줄 작업 유형 Enum
 * <p>
 * AWS EventBridge 스케줄 작업의 종류를 정의합니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>작업 유형 타입 안정성 제공</li>
 *   <li>EventType 문자열 하드코딩 방지</li>
 *   <li>작업별 고유 식별자 생성</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public enum ScheduleOperation {

    /**
     * 스케줄 생성
     */
    CREATE("SCHEDULE.CREATE.REQUEST", "create"),

    /**
     * 스케줄 수정
     */
    UPDATE("SCHEDULE.UPDATE.REQUEST", "update"),

    /**
     * 스케줄 삭제
     */
    DELETE("SCHEDULE.DELETE.REQUEST", "delete");

    private final String eventType;
    private final String action;

    /**
     * 생성자
     *
     * @param eventType Orchestrator EventType
     * @param action    Payload에 사용되는 action 문자열
     */
    ScheduleOperation(String eventType, String action) {
        this.eventType = eventType;
        this.action = action;
    }

    /**
     * Orchestrator EventType 반환
     *
     * @return EventType 문자열
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Payload action 문자열 반환
     *
     * @return action 문자열 (소문자)
     */
    public String getAction() {
        return action;
    }

    /**
     * EventBridge Executor가 사용하는 operation 문자열 반환
     * <p>
     * EventBridgeExecutor는 "CREATE", "UPDATE", "DELETE" 대문자를 기대합니다.
     * </p>
     *
     * @return operation 문자열 (대문자)
     */
    public String getOperationName() {
        return this.name();
    }
}
