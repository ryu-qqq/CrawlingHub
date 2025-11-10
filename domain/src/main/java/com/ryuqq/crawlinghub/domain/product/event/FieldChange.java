package com.ryuqq.crawlinghub.domain.product.event;

import java.util.Objects;

/**
 * 필드 변경 내역 Value Object
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record FieldChange(
    String fieldName,
    Object oldValue,
    Object newValue
) {
    public FieldChange {
        if (fieldName == null || fieldName.isBlank()) {
            throw new IllegalArgumentException("필드명은 필수입니다");
        }
    }

    /**
     * 실제로 값이 변경되었는지 확인
     */
    public boolean isActuallyChanged() {
        return !Objects.equals(oldValue, newValue);
    }
}

