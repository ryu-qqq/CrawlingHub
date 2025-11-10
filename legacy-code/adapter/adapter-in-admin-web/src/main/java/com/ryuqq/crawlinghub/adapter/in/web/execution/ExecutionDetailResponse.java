package com.ryuqq.crawlinghub.adapter.in.web.execution;

import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for execution detail
 * Contains comprehensive information including statistics and relationships
 */
public record ExecutionDetailResponse(
        Long executionId,
        String executionName,
        ScheduleInfo schedule,
        WorkflowInfo workflow,
        ExecutionStatus status,
        ExecutionStatistics statistics,
        List<ResultSummary> resultSummaries,
        List<S3PathInfo> s3Paths,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
    public record ScheduleInfo(
            Long scheduleId,
            String scheduleName
    ) {}

    public record WorkflowInfo(
            Long workflowId,
            String workflowName
    ) {}

    public record ExecutionStatistics(
            Integer totalTasks,
            Integer completedTasks,
            Integer failedTasks,
            Integer pendingTasks,
            Integer runningTasks,
            Integer totalApiCalls,
            Integer successfulApiCalls,
            Integer failedApiCalls,
            Long totalDataSizeBytes,
            Long totalItemsProcessed
    ) {}

    public record ResultSummary(
            String metricKey,
            String metricValue,
            String metricType
    ) {}

    public record S3PathInfo(
            String pathType,
            String s3Bucket,
            String s3Key,
            Long fileSizeBytes
    ) {}

    public static ExecutionDetailResponse from(
            CrawlExecution execution,
            String scheduleName,
            String workflowName,
            ExecutionStatistics statistics,
            List<ResultSummary> resultSummaries,
            List<S3PathInfo> s3Paths
    ) {
        ScheduleInfo scheduleInfo = new ScheduleInfo(
                execution.getScheduleId() != null ? execution.getScheduleId().value() : null,
                scheduleName
        );

        WorkflowInfo workflowInfo = new WorkflowInfo(
                null, // Will be populated from schedule
                workflowName
        );

        return new ExecutionDetailResponse(
                execution.getExecutionId() != null ? execution.getExecutionId().value() : null,
                execution.getExecutionName(),
                scheduleInfo,
                workflowInfo,
                execution.getStatus(),
                statistics,
                resultSummaries,
                s3Paths,
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getErrorMessage()
        );
    }
}
