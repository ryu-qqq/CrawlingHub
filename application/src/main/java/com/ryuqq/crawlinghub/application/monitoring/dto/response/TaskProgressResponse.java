package com.ryuqq.crawlinghub.application.monitoring.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 태스크 진행 상황 Response
 *
 * @param sellerId 셀러 ID
 * @param sellerName 셀러 이름
 * @param totalTasks 총 태스크 수
 * @param completedCount 완료 개수
 * @param processingCount 처리 중 개수
 * @param waitingCount 대기 중 개수
 * @param failedCount 실패 개수
 * @param taskBreakdown 태스크 유형별 세부 내역
 * @param lastUpdatedAt 마지막 업데이트 시간
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record TaskProgressResponse(
    Long sellerId,
    String sellerName,
    int totalTasks,
    int completedCount,
    int processingCount,
    int waitingCount,
    int failedCount,
    List<TaskTypeBreakdown> taskBreakdown,
    LocalDateTime lastUpdatedAt
) {
    /**
     * 진행률 계산
     */
    public double getProgressRate() {
        if (totalTasks == 0) {
            return 0.0;
        }
        return (double) completedCount / totalTasks * 100;
    }

    /**
     * 태스크 유형별 세부 내역
     */
    public record TaskTypeBreakdown(
        String taskType,
        int total,
        int completed,
        int processing,
        int waiting,
        int failed
    ) {
        public double getCompletionRate() {
            if (total == 0) {
                return 0.0;
            }
            return (double) completed / total * 100;
        }
    }
}
