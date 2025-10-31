package com.ryuqq.crawlinghub.domain.product;

/**
 * 상품 완성 상태
 */
public enum CompletionStatus {
    INCOMPLETE(1, "미완성"),
    COMPLETE(2, "완성");

    private final int priority;
    private final String description;

    CompletionStatus(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public boolean isComplete() {
        return this == COMPLETE;
    }

    public static CompletionStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("CompletionStatus는 필수입니다");
        }

        try {
            return CompletionStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 CompletionStatus입니다: " + statusStr);
        }
    }
}
