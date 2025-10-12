package com.ryuqq.crawlinghub.adapter.in.web.execution;

import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;

import java.time.Duration;
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
        String elapsedTime = calculateElapsedTime(execution.getStartedAt());

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

    private static String calculateElapsedTime(LocalDateTime startedAt) {
        if (startedAt == null) {
            return "0m 0s";
        }

        Duration elapsed = Duration.between(startedAt, LocalDateTime.now());
        long minutes = elapsed.toMinutes();
        long seconds = elapsed.minusMinutes(minutes).getSeconds();

        return String.format("%dm %ds", minutes, seconds);
    }

    public static String formatDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return "0m 0s";
        }

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        return String.format("%dm %ds", minutes, seconds);
    }
}
