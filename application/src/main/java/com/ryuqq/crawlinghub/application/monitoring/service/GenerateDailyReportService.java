package com.ryuqq.crawlinghub.application.monitoring.service;


import com.ryuqq.crawlinghub.application.monitoring.dto.query.DailyReportQuery;
import com.ryuqq.crawlinghub.application.monitoring.dto.response.DailyReportResponse;
import com.ryuqq.crawlinghub.application.monitoring.port.in.GenerateDailyReportUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.CrawlingStatsPort;
import com.ryuqq.crawlinghub.application.monitoring.port.out.LoadSellerStatsPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 일일 리포트 생성 UseCase 구현체
 *
 * <p>전체 시스템의 일일 크롤링 리포트를 생성하고 이슈 및 권장사항을 제공합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class GenerateDailyReportService implements GenerateDailyReportUseCase {

    private final CrawlingStatsPort crawlingStatsPort;
    private final LoadSellerStatsPort loadSellerStatsPort;

    public GenerateDailyReportService(
        CrawlingStatsPort crawlingStatsPort,
        LoadSellerStatsPort loadSellerStatsPort
    ) {
        this.crawlingStatsPort = crawlingStatsPort;
        this.loadSellerStatsPort = loadSellerStatsPort;
    }

    /**
     * 일일 리포트 생성
     *
     * <p>실행 순서:
     * 1. 전체 통계 조회
     * 2. 셀러별 통계 조회
     * 3. 이슈 감지
     * 4. 개선 권장사항 생성
     *
     * @param query 리포트 조회 Query
     * @return 일일 리포트
     */
    @Override
    @Transactional(readOnly = true)
    public DailyReportResponse execute(DailyReportQuery query) {
        // 1. 전체 통계 조회
        CrawlingStatsPort.TaskStats taskStats = crawlingStatsPort.getAllTaskStats(
            query.targetDate(),
            query.targetDate()
        );

        CrawlingStatsPort.ProductStats productStats = crawlingStatsPort.getAllProductStats(
            query.targetDate(),
            query.targetDate()
        );

        // 2. 전체 통계 응답 생성
        DailyReportResponse.OverallStats overallStats = new DailyReportResponse.OverallStats(
            taskStats.totalTasks(),
            taskStats.completedTasks(),
            taskStats.failedTasks(),
            calculateSuccessRate(taskStats.completedTasks(), taskStats.totalTasks()),
            productStats.totalProducts(),
            productStats.completedProducts(),
            calculateSuccessRate(productStats.completedProducts(), productStats.totalProducts())
        );

        // 3. 셀러별 통계 조회
        List<LoadSellerStatsPort.SellerDailyStats> sellerDailyStatsList =
            loadSellerStatsPort.getActiveSellerStats(query.targetDate());

        List<DailyReportResponse.SellerStats> sellerStats = sellerDailyStatsList.stream()
            .map(stats -> new DailyReportResponse.SellerStats(
                stats.sellerId(),
                stats.sellerName(),
                stats.totalTasks(),
                stats.completedTasks(),
                stats.failedTasks(),
                stats.getSuccessRate()
            ))
            .toList();

        // 4. 이슈 감지
        List<String> topIssues = detectIssues(overallStats, sellerStats);

        // 5. 개선 권장사항 생성
        List<String> recommendations = generateRecommendations(overallStats, sellerStats);

        // 6. 리포트 반환
        return new DailyReportResponse(
            query.targetDate(),
            overallStats,
            sellerStats,
            topIssues,
            recommendations
        );
    }

    /**
     * 성공률 계산
     */
    private double calculateSuccessRate(long completed, long total) {
        if (total == 0) {
            return 0.0;
        }
        return (double) completed / total * 100;
    }

    /**
     * 이슈 감지
     */
    private List<String> detectIssues(
        DailyReportResponse.OverallStats overallStats,
        List<DailyReportResponse.SellerStats> sellerStats
    ) {
        List<String> issues = new ArrayList<>();

        // 전체 성공률이 낮은 경우
        if (overallStats.successRate() < 80.0) {
            issues.add(String.format(
                "⚠️ 전체 태스크 성공률이 낮습니다: %.2f%% (목표: 80%% 이상)",
                overallStats.successRate()
            ));
        }

        // 실패 태스크가 많은 경우
        if (overallStats.failedTasks() > 100) {
            issues.add(String.format(
                "⚠️ 실패 태스크가 과도하게 많습니다: %d건",
                overallStats.failedTasks()
            ));
        }

        // 상품 완료율이 낮은 경우
        if (overallStats.productCompletionRate() < 70.0) {
            issues.add(String.format(
                "⚠️ 상품 완료율이 낮습니다: %.2f%% (목표: 70%% 이상)",
                overallStats.productCompletionRate()
            ));
        }

        // 특정 셀러의 성공률이 매우 낮은 경우
        sellerStats.stream()
            .filter(stats -> stats.successRate() < 50.0)
            .forEach(stats -> issues.add(String.format(
                "🚨 셀러 '%s'의 성공률이 매우 낮습니다: %.2f%%",
                stats.sellerName(),
                stats.successRate()
            )));

        if (issues.isEmpty()) {
            issues.add("✅ 특별한 이슈가 감지되지 않았습니다");
        }

        return issues;
    }

    /**
     * 개선 권장사항 생성
     */
    private List<String> generateRecommendations(
        DailyReportResponse.OverallStats overallStats,
        List<DailyReportResponse.SellerStats> sellerStats
    ) {
        List<String> recommendations = new ArrayList<>();

        // 성공률 기반 권장사항
        if (overallStats.successRate() < 80.0) {
            recommendations.add("💡 실패 태스크 로그를 분석하여 공통 실패 원인을 파악하세요");
            recommendations.add("💡 재시도 정책을 검토하고 최적화하세요");
        }

        // 상품 완료율 기반 권장사항
        if (overallStats.productCompletionRate() < 70.0) {
            recommendations.add("💡 미완성 상품의 크롤링 단계를 확인하세요 (미니샵/상세/옵션)");
            recommendations.add("💡 크롤링 순서를 최적화하여 완료율을 높이세요");
        }

        // 셀러별 권장사항
        long lowPerformanceSellerCount = sellerStats.stream()
            .filter(stats -> stats.successRate() < 70.0)
            .count();

        if (lowPerformanceSellerCount > 0) {
            recommendations.add(String.format(
                "💡 성능이 낮은 셀러 %d개의 설정을 점검하세요",
                lowPerformanceSellerCount
            ));
        }

        if (recommendations.isEmpty()) {
            recommendations.add("✅ 현재 시스템은 정상적으로 운영 중입니다");
        }

        return recommendations;
    }
}
