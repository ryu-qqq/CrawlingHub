package com.ryuqq.crawlinghub.application.task.dto.response;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.Instant;
import java.util.List;

/**
 * Task 상세 + Execution 이력 Response DTO
 *
 * <p>어드민용 Task 상세 조회 응답입니다. Task 정보와 최근 실행 이력을 함께 포함합니다.
 *
 * @param task Task 상세 정보
 * @param executionHistory 최근 실행 이력 목록
 * @author development-team
 * @since 1.0.0
 */
public record TaskWithExecutionsResponse(
        TaskInfo task, List<ExecutionHistoryItem> executionHistory) {

    /** Task 정보 */
    public record TaskInfo(
            Long crawlTaskId,
            Long crawlSchedulerId,
            Long sellerId,
            String status,
            String taskType,
            int retryCount,
            String baseUrl,
            String path,
            String fullUrl,
            Instant createdAt,
            Instant updatedAt) {}

    /** Execution 이력 항목 */
    public record ExecutionHistoryItem(
            Long executionId,
            CrawlExecutionStatus status,
            Integer httpStatusCode,
            String errorMessage,
            Long durationMs,
            Instant startedAt,
            Instant completedAt) {}
}
