package com.ryuqq.crawlinghub.domain.schedule.event;

/**
 * Domain event published when a schedule is updated
 * Contains information needed to update EventBridge rule
 *
 * @param scheduleId The ID of the schedule that was updated
 * @param ruleName The EventBridge rule name
 * @param cronExpression The AWS cron expression for the schedule
 * @param description The description for the EventBridge rule
 * @author crawlinghub (noreply@crawlinghub.com)
 */
public record ScheduleUpdatedEvent(
        Long scheduleId,
        String ruleName,
        String cronExpression,
        String description
) {
    /**
     * Compact constructor with validation.
     */
    public ScheduleUpdatedEvent {
        if (scheduleId == null) {
            throw new IllegalArgumentException("Schedule ID cannot be null");
        }
        if (ruleName == null || ruleName.isBlank()) {
            throw new IllegalArgumentException("Rule name cannot be null or blank");
        }
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("Cron expression cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank");
        }
    }
}
