package com.ryuqq.crawlinghub.domain.schedule.event;

/**
 * Domain event published when a schedule is disabled
 * Contains information needed to disable EventBridge rule
 *
 * @param scheduleId The ID of the schedule that was disabled
 * @param ruleName The EventBridge rule name to disable
 * @author crawlinghub (noreply@crawlinghub.com)
 */
public record ScheduleDisabledEvent(
        Long scheduleId,
        String ruleName
) {
    /**
     * Compact constructor with validation.
     */
    public ScheduleDisabledEvent {
        if (scheduleId == null) {
            throw new IllegalArgumentException("Schedule ID cannot be null");
        }
        if (ruleName == null || ruleName.isBlank()) {
            throw new IllegalArgumentException("Rule name cannot be null or blank");
        }
    }
}
