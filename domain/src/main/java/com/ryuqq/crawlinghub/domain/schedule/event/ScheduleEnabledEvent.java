package com.ryuqq.crawlinghub.domain.schedule.event;

/**
 * Domain event published when a schedule is enabled
 * Contains all information needed to create and enable EventBridge rule
 *
 * @param scheduleId The ID of the schedule that was enabled
 * @param ruleName The EventBridge rule name to create
 * @param cronExpression The AWS cron expression for the schedule
 * @param scheduleName The name of the schedule for description
 * @param targetInput The JSON input to pass to the EventBridge target
 * @author crawlinghub (noreply@crawlinghub.com)
 */
public record ScheduleEnabledEvent(
        Long scheduleId,
        String ruleName,
        String cronExpression,
        String scheduleName,
        String targetInput
) {
    /**
     * Compact constructor with validation.
     */
    public ScheduleEnabledEvent {
        if (scheduleId == null) {
            throw new IllegalArgumentException("Schedule ID cannot be null");
        }
        if (ruleName == null || ruleName.isBlank()) {
            throw new IllegalArgumentException("Rule name cannot be null or blank");
        }
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("Cron expression cannot be null or blank");
        }
        if (scheduleName == null || scheduleName.isBlank()) {
            throw new IllegalArgumentException("Schedule name cannot be null or blank");
        }
        if (targetInput == null || targetInput.isBlank()) {
            throw new IllegalArgumentException("Target input cannot be null or blank");
        }
    }
}
