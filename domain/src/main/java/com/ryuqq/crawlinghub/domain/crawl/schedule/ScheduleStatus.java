package com.ryuqq.crawlinghub.domain.crawl.schedule;

/**
 * 스케줄 상태
 */
public enum ScheduleStatus {
    ACTIVE(1, "활성"),
    SUSPENDED(2, "일시정지");

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

    public boolean isActive() {
        return this == ACTIVE;
    }

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
