package com.ryuqq.crawlinghub.application.monitoring.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * 일일 리포트 Response
 *
 * @param reportDate 리포트 날짜
 * @param overallStats 전체 통계
 * @param sellerStats 셀러별 통계
 * @param topIssues 주요 이슈 목록
 * @param recommendations 개선 권장사항
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record DailyReportResponse(
    LocalDate reportDate,
    OverallStats overallStats,
    List<SellerStats> sellerStats,
    List<String> topIssues,
    List<String> recommendations
) {
    /**
     * 전체 통계
     */
    public record OverallStats(
        long totalTasks,
        long completedTasks,
        long failedTasks,
        double successRate,
        long totalProducts,
        long completedProducts,
        double productCompletionRate
    ) {
    }

    /**
     * 셀러별 통계
     */
    public record SellerStats(
        Long sellerId,
        String sellerName,
        long totalTasks,
        long completedTasks,
        long failedTasks,
        double successRate
    ) {
    }

    /**
     * 리포트 요약
     */
    public String getSummary() {
        return String.format(
            "[%s] 전체 태스크: %d, 성공률: %.2f%%, 상품 완료율: %.2f%%",
            reportDate,
            overallStats.totalTasks(),
            overallStats.successRate(),
            overallStats.productCompletionRate()
        );
    }
}
