package com.ryuqq.crawlinghub.domain.seller.vo;

/** 머스트잇 셀러 식별자 VO. */
public record MustItSellerId(Long value) {

    public static MustItSellerId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("mustItSellerId must not be null");
        }
        return new MustItSellerId(value);
    }

    public static MustItSellerId forNew() {
        return new MustItSellerId(null);
    }

    public boolean isNew() {
        return value == null;
    }
}
