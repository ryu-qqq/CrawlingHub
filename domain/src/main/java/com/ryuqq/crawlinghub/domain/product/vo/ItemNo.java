package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 상품 번호 Value Object
 *
 * <p>외부 시스템(머스트잇)의 상품 고유 번호를 표현합니다.</p>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>양수(positive)여야 합니다</li>
 *   <li>null 불가</li>
 * </ul>
 *
 * @param value 상품 번호 (양수)
 */
public record ItemNo(Long value) {

    /**
     * Compact constructor - 상품 번호 검증
     *
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public ItemNo {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ItemNo는 양수여야 합니다");
        }
    }

    /**
     * 정적 팩토리 메서드 - ItemNo 생성
     *
     * @param value 상품 번호 (양수)
     * @return ItemNo 인스턴스
     */
    public static ItemNo of(Long value) {
        return new ItemNo(value);
    }
}
