package com.ryuqq.crawlinghub.domain.seller.vo;

public record SellerName(String value) {

    public static SellerName of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("셀러 이름은 null이거나 빈 문자열일 수 없습니다.");
        }

        return new SellerName(value);
    }
}
