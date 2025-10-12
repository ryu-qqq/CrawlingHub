package com.ryuqq.crawlinghub.adapter.in.web.execution;

import com.ryuqq.crawlinghub.adapter.in.web.util.DurationFormatter;
import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;

import java.time.LocalDateTime;

/**
 * Response DTO for execution progress
 * Used for real-time progress monitoring
 */
public record ExecutionProgressResponse(
        Long executionId,
        ExecutionStatus status,
        Double progress,
        CurrentStepInfo currentStep,
        ProgressStatistics statistics,
        String estimatedTimeRemaining,
        LocalDateTime startedAt,
        String elapsedTime
) {
    public record CurrentStepInfo(
            Long stepId,
            String stepName,
            Integer stepOrder
    ) {}

    public record ProgressStatistics(
            Integer totalTasks,
            Integer completedTasks,
            Integer failedTasks,
            Integer pendingTasks,
            Integer runningTasks
    ) {}

    public static ExecutionProgressResponse from(
            CrawlExecution execution,
            Double progress,
            CurrentStepInfo currentStep,
            ProgressStatistics statistics,
            String estimatedTimeRemaining
    ) {
        String elapsedTime = DurationFormatter.formatShortDuration(
                execution.getStartedAt(),
                null  // null means use current time
        );
        if (elapsedTime == null) {
            elapsedTime = "0m 0s";
        }

        return new ExecutionProgressResponse(
                execution.getExecutionId() != null ? execution.getExecutionId().value() : null,
                execution.getStatus(),
                progress,
                currentStep,
                statistics,
                estimatedTimeRemaining,
                execution.getStartedAt(),
                elapsedTime
        );
    }
}
