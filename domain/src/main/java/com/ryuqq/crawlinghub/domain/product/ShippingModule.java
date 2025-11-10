package com.ryuqq.crawlinghub.domain.product;

/**
 * 배송 모듈 Value Object
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record ShippingModule(
    Integer shippingFee,
    String shippingMethod,
    Integer deliveryDays
) {
    public ShippingModule {
        if (shippingFee != null && shippingFee < 0) {
            throw new IllegalArgumentException("배송비는 0 이상이어야 합니다");
        }
    }
}

