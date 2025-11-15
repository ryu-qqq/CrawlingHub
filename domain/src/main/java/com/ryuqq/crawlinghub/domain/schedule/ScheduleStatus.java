package com.ryuqq.crawlinghub.domain.schedule;

/**
 * 스케줄 상태
 *
 * @author windsurf
 * @since 1.0.0
 */
public enum ScheduleStatus {
    /** 활성 상태 */
    ACTIVE(1, "활성"),

    /** 일시정지 상태 */
    SUSPENDED(2, "일시정지"),

    /** 삭제 상태 (소프트 삭제) */
    DELETED(3, "삭제");

    private final int priority;
    private final String description;

    ScheduleStatus(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 활성 상태 여부 확인
     *
     * @return 활성 상태이면 true
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 삭제 상태 여부 확인
     *
     * @return 삭제 상태이면 true
     */
    public boolean isDeleted() {
        return this == DELETED;
    }

    /**
     * 문자열로부터 ScheduleStatus 생성
     *
     * @param statusStr 상태 문자열
     * @return ScheduleStatus
     * @throws IllegalArgumentException 유효하지 않은 상태인 경우
     */
    public static ScheduleStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("ScheduleStatus는 필수입니다");
        }

        try {
            return ScheduleStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 ScheduleStatus입니다: " + statusStr);
        }
    }
}
