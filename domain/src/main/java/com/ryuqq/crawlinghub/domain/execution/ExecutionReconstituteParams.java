package com.ryuqq.crawlinghub.domain.execution;

import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;

import java.time.LocalDateTime;

/**
 * Parameter object for CrawlExecution reconstitution from database.
 * Reduces parameter count and improves method signature readability.
 */
public record ExecutionReconstituteParams(
        ExecutionId executionId,
        ScheduleId scheduleId,
        String executionName,
        ExecutionStatus status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
    public static ExecutionReconstituteParams of(
            ExecutionId executionId,
            ScheduleId scheduleId,
            String executionName,
            ExecutionStatus status,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            String errorMessage
    ) {
        return new ExecutionReconstituteParams(
                executionId, scheduleId, executionName,
                status, startedAt, completedAt, errorMessage
        );
    }
}
