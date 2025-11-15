package com.ryuqq.crawlinghub.domain.seller;

/**
 * MustitSeller 식별자
 */
public record MustItSellerId(Long value) {

    public MustItSellerId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("MustItSeller ID는 양수여야 합니다");
        }
    }

    public static MustItSellerId of(Long value) {
        return new MustItSellerId(value);
    }
}
