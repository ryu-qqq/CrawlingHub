package com.ryuqq.crawlinghub.domain.seller.vo;

/**
 * 셀러 식별자 Value Object
 *
 * <p>Auto-increment Long 기반 셀러 고유 식별자</p>
 *
 * @param value 셀러 ID (null이면 새로운 엔티티)
 */
public record SellerId(Long value) {

    /**
     * 새로운 Seller ID 생성 (null)
     *
     * @return null을 가진 SellerId (새 엔티티 표시)
     */
    public static SellerId forNew() {
        return new SellerId(null);
    }

    /**
     * 기존 ID 값으로 SellerId 생성 (정적 팩토리 메서드)
     *
     * @param value Seller ID 값
     * @return SellerId 인스턴스
     */
    public static SellerId of(Long value) {
        return new SellerId(value);
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
