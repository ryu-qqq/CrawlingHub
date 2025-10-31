package com.ryuqq.crawlinghub.application.monitoring.dto.query;

import java.time.LocalDate;

/**
 * 크롤링 통계 조회 Query
 *
 * @param sellerId 셀러 ID (null이면 전체)
 * @param startDate 시작일
 * @param endDate 종료일
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record CrawlingStatsQuery(
    Long sellerId,
    LocalDate startDate,
    LocalDate endDate
) {
    public CrawlingStatsQuery {
        if (startDate == null) {
            throw new IllegalArgumentException("시작일은 필수입니다");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("종료일은 필수입니다");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다");
        }
    }

    /**
     * 오늘 통계 조회
     */
    public static CrawlingStatsQuery today() {
        LocalDate today = LocalDate.now();
        return new CrawlingStatsQuery(null, today, today);
    }

    /**
     * 특정 셀러의 오늘 통계
     */
    public static CrawlingStatsQuery todayForSeller(Long sellerId) {
        LocalDate today = LocalDate.now();
        return new CrawlingStatsQuery(sellerId, today, today);
    }

    /**
     * 기간별 통계
     */
    public static CrawlingStatsQuery between(LocalDate startDate, LocalDate endDate) {
        return new CrawlingStatsQuery(null, startDate, endDate);
    }

    /**
     * 셀러 필터 존재 여부
     */
    public boolean hasSellerFilter() {
        return sellerId != null;
    }
}
