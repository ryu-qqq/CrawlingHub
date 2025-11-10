package com.ryuqq.crawlinghub.domain.task;

/**
 * Task 트리거 타입
 *
 * <p>Task가 어떤 방식으로 생성되었는지를 나타냅니다.
 *
 * <ul>
 *   <li>MANUAL: REST API를 통한 수동 생성</li>
 *   <li>AUTO: Event를 통한 자동 생성 (Scheduler, Event Handler 등)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public enum TriggerType {
    /**
     * 수동 트리거 (REST API)
     */
    MANUAL,

    /**
     * 자동 트리거 (Event, Scheduler)
     */
    AUTO
}
