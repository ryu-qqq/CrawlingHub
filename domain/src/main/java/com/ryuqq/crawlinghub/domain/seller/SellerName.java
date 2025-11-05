package com.ryuqq.crawlinghub.domain.seller;

import java.util.Objects;

/**
 * 셀러 이름 Value Object
 *
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>셀러 이름은 1자 이상 100자 이하</li>
 *   <li>공백만으로 구성될 수 없음</li>
 *   <li>불변 객체</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class SellerName {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;

    private final String value;

    private SellerName(String value) {
        this.value = value;
    }

    /**
     * SellerName 생성 (Factory Method)
     *
     * @param value 셀러 이름
     * @return SellerName 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 셀러 이름인 경우
     */
    public static SellerName of(String value) {
        validateSellerName(value);
        return new SellerName(value);
    }

    private static void validateSellerName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("셀러 이름은 필수입니다");
        }

        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("셀러 이름은 %d자 이상 %d자 이하여야 합니다", MIN_LENGTH, MAX_LENGTH)
            );
        }
    }

    /**
     * 셀러 이름 값 반환
     *
     * @return 셀러 이름
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
        SellerName that = (SellerName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "SellerName{" +
            "value='" + value + '\'' +
            '}';
    }
}
