package com.ryuqq.crawlinghub.domain.mustit.seller;

/**
 * 머스트잇 셀러 기본 정보 Value Object
 * <p>
 * sellerId, name, isActive를 그룹화하여 응집도를 높입니다.
 * </p>
 *
 * @param sellerId 셀러 ID (머스트잇 고유 식별자)
 * @param name     셀러명 (불변)
 * @param isActive 활성 상태
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record SellerBasicInfo(
        String sellerId,
        String name,
        boolean isActive
) {
    /**
     * 정적 팩토리 메서드 - 기본 정보 생성
     *
     * @param sellerId 셀러 ID
     * @param name     셀러명
     * @param isActive 활성 상태
     * @return SellerBasicInfo 인스턴스
     */
    public static SellerBasicInfo of(String sellerId, String name, boolean isActive) {
        return new SellerBasicInfo(sellerId, name, isActive);
    }

    /**
     * Compact constructor - 유효성 검증
     */
    public SellerBasicInfo {
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("sellerId must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
    }
}
