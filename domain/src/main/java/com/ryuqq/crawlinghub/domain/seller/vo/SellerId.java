package com.ryuqq.crawlinghub.domain.seller.vo;

/**
 * 셀러 식별자 Value Object
 *
 * <p>머스트잇 크롤링 대상 셀러의 고유 식별자를 표현합니다.</p>
 *
 * @param value 셀러 ID 문자열 (null 또는 blank 불가)
 * @throws IllegalArgumentException value가 null 또는 blank인 경우
 */
public record SellerId(String value) {

    /**
     * 생성자 - Compact Constructor로 검증 수행
     */
    public SellerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SellerId는 비어있을 수 없습니다");
        }
    }
}
