package com.ryuqq.crawlinghub.application.seller.dto.response;

import java.time.Instant;

/**
 * Seller Summary Response
 *
 * <p>셀러 목록 조회용 요약 응답 데이터
 *
 * @param sellerId 셀러 ID
 * @param mustItSellerName 머스트잇 셀러 이름
 * @param sellerName 셀러 이름
 * @param active 활성화 여부
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param activeSchedulerCount 활성 스케줄러 수
 * @param totalSchedulerCount 전체 스케줄러 수
 * @param lastTaskStatus 최근 태스크 상태
 * @param lastTaskExecutedAt 최근 태스크 실행 시각
 * @param totalProductCount 크롤링된 상품 수
 * @author development-team
 * @since 1.0.0
 */
public record SellerSummaryResponse(
        Long sellerId,
        String mustItSellerName,
        String sellerName,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        int activeSchedulerCount,
        int totalSchedulerCount,
        String lastTaskStatus,
        Instant lastTaskExecutedAt,
        long totalProductCount) {

    /** 기본 생성 메서드 (통계 정보 포함) */
    public static SellerSummaryResponse of(
            Long sellerId,
            String mustItSellerName,
            String sellerName,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            int activeSchedulerCount,
            int totalSchedulerCount,
            String lastTaskStatus,
            Instant lastTaskExecutedAt,
            long totalProductCount) {
        return new SellerSummaryResponse(
                sellerId,
                mustItSellerName,
                sellerName,
                active,
                createdAt,
                updatedAt,
                activeSchedulerCount,
                totalSchedulerCount,
                lastTaskStatus,
                lastTaskExecutedAt,
                totalProductCount);
    }
}
