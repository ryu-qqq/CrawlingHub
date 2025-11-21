package com.ryuqq.crawlinghub.domain.seller.vo;

/** Seller Aggregate의 식별자 VO. */
public record SellerId(Long value) {

    private static final SellerId NEW = new SellerId(null);

    public static SellerId forNew() {
        return NEW;
    }

    public static SellerId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("sellerId must not be null");
        }
        return new SellerId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
