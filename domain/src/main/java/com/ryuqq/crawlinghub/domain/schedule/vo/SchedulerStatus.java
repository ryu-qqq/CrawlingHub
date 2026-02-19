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
    ACTIVE("활성"),

    /** 비활성 상태 */
    INACTIVE("비활성");

    private final String displayName;

    SchedulerStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 사용자 표시용 이름 반환
     *
     * @return 표시명
     */
    public String displayName() {
        return displayName;
    }
}
