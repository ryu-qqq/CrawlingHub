package com.ryuqq.crawlinghub.domain.mustit.seller.history;

/**
 * ProductCountHistoryId - 상품 수 이력 식별자
 *
 * <p>Record 패턴 사용 (Java 21)</p>
 *
 * @param value Long FK (ID)
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ProductCountHistoryId(Long value) {

    /**
     * Compact Constructor - 검증 로직
     */
    public ProductCountHistoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductCountHistoryId는 양수여야 합니다");
        }
    }

    /**
     * Factory Method
     *
     * @param value Long ID 값
     * @return ProductCountHistoryId
     */
    public static ProductCountHistoryId of(Long value) {
        return new ProductCountHistoryId(value);
    }
}

