package com.ryuqq.crawlinghub.domain.change;

/**
 * ChangeDetection 식별자
 */
public record ChangeDetectionId(Long value) {

    public ChangeDetectionId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("ChangeDetection ID는 양수여야 합니다");
        }
    }

    public static ChangeDetectionId of(Long value) {
        return new ChangeDetectionId(value);
    }
}
