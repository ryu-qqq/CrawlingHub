package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

/**
 * Seller Detail Statistics API Response
 *
 * <p>셀러 상세 조회 시 포함되는 상세 통계 정보
 *
 * @param totalProducts 전체 상품 수
 * @param syncedProducts 동기화 완료 상품 수
 * @param pendingSyncProducts 동기화 대기 상품 수
 * @param successRate 크롤링 성공률 (0.0 ~ 1.0)
 * @author development-team
 * @since 1.0.0
 */
public record SellerDetailStatisticsApiResponse(
        long totalProducts, long syncedProducts, long pendingSyncProducts, double successRate) {}
