package com.ryuqq.crawlinghub.application.monitoring.dto.query;

import java.time.LocalDate;

/**
 * 일일 리포트 조회 Query
 *
 * @param targetDate 리포트 대상 날짜
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record DailyReportQuery(
    LocalDate targetDate
) {
    public DailyReportQuery {
        if (targetDate == null) {
            throw new IllegalArgumentException("리포트 날짜는 필수입니다");
        }
    }

    /**
     * 오늘 리포트
     */
    public static DailyReportQuery today() {
        return new DailyReportQuery(LocalDate.now());
    }

    /**
     * 어제 리포트
     */
    public static DailyReportQuery yesterday() {
        return new DailyReportQuery(LocalDate.now().minusDays(1));
    }
}
