package com.ryuqq.crawlinghub.adapter.in.web.execution;

import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Response DTO for execution summary
 * Used in list views - contains only essential information
 */
public record ExecutionSummaryResponse(
        Long executionId,
        String executionName,
        Long scheduleId,
        String scheduleName,
        String workflowName,
        ExecutionStatus status,
        Double progress,
        Integer totalTasks,
        Integer completedTasks,
        Integer failedTasks,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String duration
) {
    public static ExecutionSummaryResponse from(
            CrawlExecution execution,
            String scheduleName,
            String workflowName,
            Double progress,
            Integer totalTasks,
            Integer completedTasks,
            Integer failedTasks
    ) {
        String duration = calculateDuration(execution.getStartedAt(), execution.getCompletedAt());

        return new ExecutionSummaryResponse(
                execution.getExecutionId() != null ? execution.getExecutionId().value() : null,
                execution.getExecutionName(),
                execution.getScheduleId() != null ? execution.getScheduleId().value() : null,
                scheduleName,
                workflowName,
                execution.getStatus(),
                progress,
                totalTasks,
                completedTasks,
                failedTasks,
                execution.getStartedAt(),
                execution.getCompletedAt(),
                duration
        );
    }

    private static String calculateDuration(LocalDateTime startedAt, LocalDateTime completedAt) {
        if (startedAt == null) {
            return null;
        }

        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        Duration duration = Duration.between(startedAt, endTime);

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        return String.format("%dm %ds", minutes, seconds);
    }
}
