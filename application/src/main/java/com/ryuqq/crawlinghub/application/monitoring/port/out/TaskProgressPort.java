package com.ryuqq.crawlinghub.application.monitoring.port.out;

import com.ryuqq.crawlinghub.domain.crawl.task.TaskType;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

import java.util.Map;

/**
 * 태스크 진행 상황 조회 Port
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface TaskProgressPort {

    /**
     * 셀러의 태스크 진행 상황 조회
     */
    ProgressStats getProgressStats(MustitSellerId sellerId);

    /**
     * 태스크 유형별 통계
     */
    Map<TaskType, TypeStats> getTaskTypeBreakdown(MustitSellerId sellerId);

    /**
     * 진행 상황 통계
     */
    record ProgressStats(
        int totalTasks,
        int completedCount,
        int processingCount,
        int waitingCount,
        int failedCount
    ) {
    }

    /**
     * 유형별 통계
     */
    record TypeStats(
        int total,
        int completed,
        int processing,
        int waiting,
        int failed
    ) {
    }
}
