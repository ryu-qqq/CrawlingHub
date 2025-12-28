package com.ryuqq.crawlinghub.application.seller.dto.response;

/**
 * Seller Detail Statistics
 *
 * <p>셀러 상세 조회 시 포함되는 상세 통계 정보
 *
 * @param totalProducts 전체 크롤링 상품 수
 * @param syncedProducts 동기화 완료 상품 수
 * @param pendingSyncProducts 동기화 대기 상품 수
 * @param successRate 성공률 (0.0 ~ 1.0)
 * @author development-team
 * @since 1.0.0
 */
public record SellerDetailStatistics(
        long totalProducts, long syncedProducts, long pendingSyncProducts, double successRate) {

    /** 통계 정보 없음을 나타내는 기본 인스턴스 */
    public static SellerDetailStatistics empty() {
        return new SellerDetailStatistics(0L, 0L, 0L, 0.0);
    }
}
