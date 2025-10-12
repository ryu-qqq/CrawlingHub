package com.ryuqq.crawlinghub.adapter.in.web.execution;

import com.ryuqq.crawlinghub.adapter.in.web.util.DurationFormatter;
import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;

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
        String duration = DurationFormatter.formatShortDuration(
                task.getStartedAt(),
                task.getCompletedAt()
        );

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
}
