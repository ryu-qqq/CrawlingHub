package com.ryuqq.crawlinghub.domain.product;

/**
 * 상품 옵션 Value Object
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record ProductOption(
    String optionName,
    String optionValue,
    Integer stock,
    Long additionalPrice
) {
    public ProductOption {
        if (optionName == null || optionName.isBlank()) {
            throw new IllegalArgumentException("옵션명은 필수입니다");
        }
        if (stock != null && stock < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다");
        }
    }
}

