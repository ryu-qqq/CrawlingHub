package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * Product 식별자 Value Object
 *
 * <p>Auto-increment Long 기반 Product 고유 식별자</p>
 *
 * @param value Product ID (null이면 새로운 엔티티)
 */
public record ProductId(Long value) {

    /**
     * 새로운 Product ID 생성 (null)
     *
     * @return null을 가진 ProductId (새 엔티티 표시)
     */
    public static ProductId forNew() {
        return new ProductId(null);
    }

    /**
     * 기존 ID 값으로 ProductId 생성 (정적 팩토리 메서드)
     *
     * @param value Product ID 값
     * @return ProductId 인스턴스
     */
    public static ProductId of(Long value) {
        return new ProductId(value);
    }

    /**
     * 새로운 엔티티인지 확인
     *
     * @return value가 null이면 true (아직 DB에 저장되지 않음)
     */
    public boolean isNew() {
        return value == null;
    }
}
