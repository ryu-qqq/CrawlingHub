package com.ryuqq.crawlinghub.domain.mustit.seller;

/**
 * MustitSeller 식별자
 */
public record MustitSellerId(Long value) {

    public MustitSellerId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("MustitSeller ID는 양수여야 합니다");
        }
    }

    public static MustitSellerId of(Long value) {
        return new MustitSellerId(value);
    }
}
