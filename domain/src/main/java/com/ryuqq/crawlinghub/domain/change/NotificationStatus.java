package com.ryuqq.crawlinghub.domain.change;

/**
 * 알림 상태
 */
public enum NotificationStatus {
    PENDING(1, "대기"),
    SENT(2, "전송됨"),
    FAILED(3, "실패");

    private final int priority;
    private final String description;

    NotificationStatus(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSent() {
        return this == SENT;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public static NotificationStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("NotificationStatus는 필수입니다");
        }

        try {
            return NotificationStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 NotificationStatus입니다: " + statusStr);
        }
    }
}
