package com.ryuqq.crawlinghub.domain.seller.vo;

public record MustItSellerName(String value) {

    public static MustItSellerName of(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("머스트잇 셀러 이름은 null이거나 빈 문자열일 수 없습니다.");
        }

        return new MustItSellerName(value);
    }
}
