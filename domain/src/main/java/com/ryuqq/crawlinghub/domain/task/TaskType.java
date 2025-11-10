package com.ryuqq.crawlinghub.domain.task;

/**
 * 크롤링 작업 유형
 */
public enum TaskType {
    META(0, "메타"),
    MINI_SHOP(1, "미니샵"),
    PRODUCT_DETAIL(2, "상품 상세"),
    PRODUCT_OPTION(3, "상품 옵션");

    private final int priority;
    private final String description;

    TaskType(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public static TaskType fromString(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) {
            throw new IllegalArgumentException("TaskType은 필수입니다");
        }

        try {
            return TaskType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 TaskType입니다: " + typeStr);
        }
    }
}
