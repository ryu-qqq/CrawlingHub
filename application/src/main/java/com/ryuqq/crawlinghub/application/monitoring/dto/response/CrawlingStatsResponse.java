package com.ryuqq.crawlinghub.application.monitoring.dto.response;

import java.time.LocalDate;

/**
 * 크롤링 통계 Response
 *
 * @param sellerId 셀러 ID (전체 통계인 경우 null)
 * @param startDate 시작일
 * @param endDate 종료일
 * @param totalTasks 총 태스크 수
 * @param completedTasks 완료된 태스크 수
 * @param failedTasks 실패한 태스크 수
 * @param processingTasks 처리 중인 태스크 수
 * @param waitingTasks 대기 중인 태스크 수
 * @param totalProducts 총 수집 상품 수
 * @param completedProducts 완료된 상품 수 (미니샵+상세+옵션 모두)
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record CrawlingStatsResponse(
    Long sellerId,
    LocalDate startDate,
    LocalDate endDate,
    long totalTasks,
    long completedTasks,
    long failedTasks,
    long processingTasks,
    long waitingTasks,
    long totalProducts,
    long completedProducts
) {
    /**
     * 태스크 성공률 계산
     */
    public double getTaskSuccessRate() {
        if (totalTasks == 0) {
            return 0.0;
        }
        return (double) completedTasks / totalTasks * 100;
    }

    /**
     * 상품 완료율 계산
     */
    public double getProductCompletionRate() {
        if (totalProducts == 0) {
            return 0.0;
        }
        return (double) completedProducts / totalProducts * 100;
    }

    /**
     * 전체 통계 여부
     */
    public boolean isOverallStats() {
        return sellerId == null;
    }
}
