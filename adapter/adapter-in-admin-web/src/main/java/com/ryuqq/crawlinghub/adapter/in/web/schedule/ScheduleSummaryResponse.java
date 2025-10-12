package com.ryuqq.crawlinghub.adapter.in.web.schedule;

import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;

import java.time.LocalDateTime;

/**
 * Response DTO for schedule summary
 * Used in list views - contains only essential information
 */
public record ScheduleSummaryResponse(
        Long scheduleId,
        Long workflowId,
        String scheduleName,
        String cronExpression,
        boolean isEnabled,
        LocalDateTime nextExecutionTime
) {
    public static ScheduleSummaryResponse from(CrawlSchedule schedule) {
        return new ScheduleSummaryResponse(
                schedule.getScheduleId().value(),
                schedule.getWorkflowId().value(),
                schedule.getScheduleName(),
                schedule.getCronExpression(),
                schedule.isEnabled(),
                schedule.getNextExecutionTime()
        );
    }
}
