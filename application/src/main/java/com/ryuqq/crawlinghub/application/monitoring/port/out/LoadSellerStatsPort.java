package com.ryuqq.crawlinghub.application.monitoring.port.out;

import java.time.LocalDate;
import java.util.List;

/**
 * 셀러 통계 조회 Port
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface LoadSellerStatsPort {

    /**
     * 활성 셀러별 통계 조회
     */
    List<SellerDailyStats> getActiveSellerStats(LocalDate targetDate);

    /**
     * 셀러 일일 통계
     */
    record SellerDailyStats(
        Long sellerId,
        String sellerName,
        long totalTasks,
        long completedTasks,
        long failedTasks
    ) {
        public double getSuccessRate() {
            if (totalTasks == 0) {
                return 0.0;
            }
            return (double) completedTasks / totalTasks * 100;
        }
    }
}
