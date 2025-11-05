package com.ryuqq.crawlinghub.domain.product;

/**
 * Product 식별자
 */
public record ProductId(Long value) {

    public ProductId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Product ID는 양수여야 합니다");
        }
    }

    public static ProductId of(Long value) {
        return new ProductId(value);
    }
}
