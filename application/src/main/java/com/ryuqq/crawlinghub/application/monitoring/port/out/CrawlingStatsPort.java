package com.ryuqq.crawlinghub.application.monitoring.port.out;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

import java.time.LocalDate;

/**
 * 크롤링 통계 조회 Port
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface CrawlingStatsPort {

    /**
     * 전체 태스크 통계 조회
     */
    TaskStats getTaskStats(MustitSellerId sellerId, LocalDate startDate, LocalDate endDate);

    /**
     * 전체 통계 (셀러 필터 없음)
     */
    TaskStats getAllTaskStats(LocalDate startDate, LocalDate endDate);

    /**
     * 상품 통계 조회
     */
    ProductStats getProductStats(MustitSellerId sellerId, LocalDate startDate, LocalDate endDate);

    /**
     * 전체 상품 통계
     */
    ProductStats getAllProductStats(LocalDate startDate, LocalDate endDate);

    /**
     * 태스크 통계
     */
    record TaskStats(
        long totalTasks,
        long completedTasks,
        long failedTasks,
        long processingTasks,
        long waitingTasks
    ) {
    }

    /**
     * 상품 통계
     */
    record ProductStats(
        long totalProducts,
        long completedProducts
    ) {
    }
}
