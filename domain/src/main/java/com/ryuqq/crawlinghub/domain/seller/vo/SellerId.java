package com.ryuqq.crawlinghub.domain.seller.vo;

public record SellerId(Long value) {

    public static SellerId forNew() {
        return new SellerId(null);
    }

    public static SellerId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("SellerId value is null");
        }
        return new SellerId(value);
    }
}
