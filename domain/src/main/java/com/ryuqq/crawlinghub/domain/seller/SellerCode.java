package com.ryuqq.crawlinghub.domain.seller;

import java.util.Objects;

/**
 * 셀러 코드 Value Object
 *
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>셀러 코드는 필수</li>
 *   <li>공백 불가</li>
 *   <li>불변 객체</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class SellerCode {

    private final String value;

    private SellerCode(String value) {
        this.value = value;
    }

    /**
     * SellerCode 생성 (Factory Method)
     *
     * @param value 셀러 코드
     * @return SellerCode 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 셀러 코드인 경우
     */
    public static SellerCode of(String value) {
        validateSellerCode(value);
        return new SellerCode(value);
    }

    private static void validateSellerCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("셀러 코드는 필수입니다");
        }
    }

    /**
     * 셀러 코드 값 반환
     *
     * @return 셀러 코드
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SellerCode that = (SellerCode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "SellerCode{" +
            "value='" + value + '\'' +
            '}';
    }
}
