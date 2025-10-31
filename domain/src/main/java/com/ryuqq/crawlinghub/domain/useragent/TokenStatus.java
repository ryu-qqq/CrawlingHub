package com.ryuqq.crawlinghub.domain.useragent;

/**
 * 토큰 상태
 */
public enum TokenStatus {
    IDLE(1, "유휴"),
    ACTIVE(2, "활성"),
    RATE_LIMITED(3, "속도제한"),
    DISABLED(4, "비활성"),
    RECOVERED(5, "복구됨");

    private final int priority;
    private final String description;

    TokenStatus(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public boolean canMakeRequest() {
        return this == IDLE || this == ACTIVE || this == RECOVERED;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

    public boolean isRateLimited() {
        return this == RATE_LIMITED;
    }

    public static TokenStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("TokenStatus는 필수입니다");
        }

        try {
            return TokenStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 TokenStatus입니다: " + statusStr);
        }
    }
}
