package com.ryuqq.crawlinghub.domain.product;

/**
 * 상품 정보 모듈 Value Object
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record ProductInfoModule(
    String description,
    String manufacturer,
    String origin
) {
    public ProductInfoModule {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("상품 설명은 필수입니다");
        }
    }
}

