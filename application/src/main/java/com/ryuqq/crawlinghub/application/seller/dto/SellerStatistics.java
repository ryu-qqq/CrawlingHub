package com.ryuqq.crawlinghub.application.seller.dto;

import java.time.Instant;

/**
 * Seller 통계 정보 DTO
 *
 * <p>셀러별 스케줄러, 태스크, 상품 통계를 담는 DTO입니다.
 *
 * @param activeSchedulerCount 활성 스케줄러 수
 * @param totalSchedulerCount 전체 스케줄러 수
 * @param lastTaskStatus 최근 태스크 상태
 * @param lastTaskExecutedAt 최근 태스크 실행 시각
 * @param totalProductCount 크롤링된 상품 수
 * @author development-team
 * @since 1.0.0
 */
public record SellerStatistics(
        int activeSchedulerCount,
        int totalSchedulerCount,
        String lastTaskStatus,
        Instant lastTaskExecutedAt,
        long totalProductCount) {

    /** 통계 정보 없음을 나타내는 기본 인스턴스 */
    public static SellerStatistics empty() {
        return new SellerStatistics(0, 0, null, null, 0L);
    }
}
