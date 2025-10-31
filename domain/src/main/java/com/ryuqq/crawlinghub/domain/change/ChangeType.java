package com.ryuqq.crawlinghub.domain.change;

/**
 * 변경 유형
 */
public enum ChangeType {
    PRICE(1, "가격"),
    STOCK(2, "재고"),
    OPTION(3, "옵션"),
    IMAGE(4, "이미지");

    private final int priority;
    private final String description;

    ChangeType(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public static ChangeType fromString(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) {
            throw new IllegalArgumentException("ChangeType은 필수입니다");
        }

        try {
            return ChangeType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 ChangeType입니다: " + typeStr);
        }
    }
}
