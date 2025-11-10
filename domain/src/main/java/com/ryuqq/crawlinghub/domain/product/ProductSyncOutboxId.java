package com.ryuqq.crawlinghub.domain.product;

/**
 * ProductSyncOutbox 식별자
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record ProductSyncOutboxId(Long value) {
    public ProductSyncOutboxId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("ProductSyncOutbox ID는 양수여야 합니다");
        }
    }

    public static ProductSyncOutboxId of(Long value) {
        return new ProductSyncOutboxId(value);
    }
}

