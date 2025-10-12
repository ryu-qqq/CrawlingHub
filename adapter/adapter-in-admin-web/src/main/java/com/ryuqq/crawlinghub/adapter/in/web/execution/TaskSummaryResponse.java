package com.ryuqq.crawlinghub.adapter.in.web.execution;

import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Response DTO for task summary
 * Used in task list views
 */
public record TaskSummaryResponse(
        Long taskId,
        String taskName,
        Long stepId,
        String stepName,
        TaskStatus status,
        Integer retryCount,
        LocalDateTime queuedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String duration,
        Integer apiResponseTime,
        Integer apiStatusCode,
        Long dataSizeBytes
) {
    public static TaskSummaryResponse from(CrawlTask task) {
        return from(task, "Step Name", 0, 0, 0L);
    }

    public static TaskSummaryResponse from(
            CrawlTask task,
            String stepName,
            Integer apiResponseTime,
            Integer apiStatusCode,
            Long dataSizeBytes
    ) {
        String duration = calculateDuration(task.getStartedAt(), task.getCompletedAt());

        return new TaskSummaryResponse(
                task.getTaskId() != null ? task.getTaskId().value() : null,
                task.getTaskName(),
                task.getStepId() != null ? task.getStepId().value() : null,
                stepName,
                task.getStatus(),
                task.getRetryCount(),
                task.getQueuedAt(),
                task.getStartedAt(),
                task.getCompletedAt(),
                duration,
                apiResponseTime,
                apiStatusCode,
                dataSizeBytes
        );
    }

    private static String calculateDuration(LocalDateTime startedAt, LocalDateTime completedAt) {
        if (startedAt == null) {
            return null;
        }

        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        Duration duration = Duration.between(startedAt, endTime);

        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return seconds + "s";
        }

        long minutes = duration.toMinutes();
        long remainingSeconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%dm %ds", minutes, remainingSeconds);
    }
}
