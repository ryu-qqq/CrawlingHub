package com.ryuqq.crawlinghub.domain.vo;

/**
 * 셀러 상태 Enum
 *
 * <p>머스트잇 크롤링 대상 셀러의 활성화 상태를 표현합니다.</p>
 *
 * <p>상태 종류:</p>
 * <ul>
 *   <li>{@link #ACTIVE} - 활성화 (크롤링 대상)</li>
 *   <li>{@link #INACTIVE} - 비활성화 (크롤링 제외)</li>
 * </ul>
 */
public enum SellerStatus {

    /**
     * 활성화 상태 - 크롤링 대상
     */
    ACTIVE,

    /**
     * 비활성화 상태 - 크롤링 제외
     */
    INACTIVE;

    /**
     * String 값으로부터 SellerStatus 생성 (표준 패턴)
     *
     * @param value 문자열 값
     * @return SellerStatus enum
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     */
    public static SellerStatus of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("SellerStatus cannot be null");
        }
        return valueOf(value.toUpperCase());
    }
}
