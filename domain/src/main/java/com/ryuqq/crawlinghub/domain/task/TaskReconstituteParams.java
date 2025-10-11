package com.ryuqq.crawlinghub.domain.task;

import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.workflow.StepId;

import java.time.LocalDateTime;

/**
 * Parameter object for CrawlTask reconstitution from database.
 * Groups related parameters to reduce method complexity and improve maintainability.
 */
public record TaskReconstituteParams(
        TaskId taskId,
        ExecutionId executionId,
        StepId stepId,
        TaskId parentTaskId,
        String taskName,
        TaskStatus status,
        int retryCount,
        int maxRetryCount,
        LocalDateTime queuedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
    public static TaskReconstituteParams of(
            TaskId taskId,
            ExecutionId executionId,
            StepId stepId,
            TaskId parentTaskId,
            String taskName,
            TaskStatus status,
            int retryCount,
            int maxRetryCount,
            LocalDateTime queuedAt,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            String errorMessage
    ) {
        return new TaskReconstituteParams(
                taskId, executionId, stepId, parentTaskId,
                taskName, status, retryCount, maxRetryCount,
                queuedAt, startedAt, completedAt, errorMessage
        );
    }
}
