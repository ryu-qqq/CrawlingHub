package com.ryuqq.crawlinghub.application.monitoring.assembler;

import com.ryuqq.crawlinghub.application.monitoring.dto.response.CrawlingStatsResponse;
import com.ryuqq.crawlinghub.application.monitoring.dto.response.TaskProgressResponse;
import com.ryuqq.crawlinghub.application.monitoring.port.out.CrawlingStatsPort;
import com.ryuqq.crawlinghub.application.monitoring.port.out.TaskProgressPort;
import com.ryuqq.crawlinghub.domain.crawl.task.TaskType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 모니터링 Assembler
 *
 * <p>모니터링 통계 DTO 변환 담당
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public final class MonitoringAssembler {

    private MonitoringAssembler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * 크롤링 통계 Response 생성
     */
    public static CrawlingStatsResponse toCrawlingStatsResponse(
        Long sellerId,
        LocalDate startDate,
        LocalDate endDate,
        CrawlingStatsPort.TaskStats taskStats,
        CrawlingStatsPort.ProductStats productStats
    ) {
        return new CrawlingStatsResponse(
            sellerId,
            startDate,
            endDate,
            taskStats.totalTasks(),
            taskStats.completedTasks(),
            taskStats.failedTasks(),
            taskStats.processingTasks(),
            taskStats.waitingTasks(),
            productStats.totalProducts(),
            productStats.completedProducts()
        );
    }

    /**
     * 태스크 진행 상황 Response 생성
     */
    public static TaskProgressResponse toTaskProgressResponse(
        Long sellerId,
        String sellerName,
        TaskProgressPort.ProgressStats progressStats,
        Map<TaskType, TaskProgressPort.TypeStats> typeStatsMap
    ) {
        List<TaskProgressResponse.TaskTypeBreakdown> breakdown = typeStatsMap.entrySet().stream()
            .map(entry -> new TaskProgressResponse.TaskTypeBreakdown(
                entry.getKey().name(),
                entry.getValue().total(),
                entry.getValue().completed(),
                entry.getValue().processing(),
                entry.getValue().waiting(),
                entry.getValue().failed()
            ))
            .toList();

        return new TaskProgressResponse(
            sellerId,
            sellerName,
            progressStats.totalTasks(),
            progressStats.completedCount(),
            progressStats.processingCount(),
            progressStats.waitingCount(),
            progressStats.failedCount(),
            breakdown,
            LocalDateTime.now()
        );
    }
}
