package com.ryuqq.crawlinghub.domain.product;

/**
 * ProductSnapshot 식별자
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record ProductSnapshotId(Long value) {
    public ProductSnapshotId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("ProductSnapshot ID는 양수여야 합니다");
        }
    }

    public static ProductSnapshotId of(Long value) {
        return new ProductSnapshotId(value);
    }
}

