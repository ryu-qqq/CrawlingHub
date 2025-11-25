package com.ryuqq.crawlinghub.domain.seller.identifier;

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
