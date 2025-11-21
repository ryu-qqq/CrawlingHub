package com.ryuqq.crawlinghub.domain.schedule.vo;

/**
 * 스케줄러 상태
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>ACTIVE: 활성 상태 (AWS EventBridge에서 실행)
 *   <li>INACTIVE: 비활성 상태 (AWS EventBridge에서 중지)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum SchedulerStatus {

    /** 활성 상태 */
    ACTIVE,

    /** 비활성 상태 */
    INACTIVE
}
