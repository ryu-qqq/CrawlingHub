package com.ryuqq.crawlinghub.domain.seller;

/**
 * 셀러 상태
 */
public enum SellerStatus {
    ACTIVE(1, "활성"),
    PAUSED(2, "일시정지"),
    DISABLED(3, "비활성");

    private final int priority;
    private final String description;

    SellerStatus(int priority, String description) {
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

    public boolean canCrawl() {
        return this != DISABLED;
    }

    public static SellerStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("SellerStatus는 필수입니다");
        }

        try {
            return SellerStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 SellerStatus입니다: " + statusStr);
        }
    }
}
