package com.ryuqq.crawlinghub.domain.schedule.event;

/**
 * Domain event published when a schedule is deleted
 * Contains information needed to clean up EventBridge resources
 *
 * @param scheduleId The ID of the schedule that was deleted
 * @param ruleName The EventBridge rule name to clean up
 * @param wasEnabled Whether the schedule was enabled before deletion
 * @author crawlinghub (noreply@crawlinghub.com)
 */
public record ScheduleDeletedEvent(
        Long scheduleId,
        String ruleName,
        boolean wasEnabled
) {
    /**
     * Compact constructor with validation.
     */
    public ScheduleDeletedEvent {
        if (scheduleId == null) {
            throw new IllegalArgumentException("Schedule ID cannot be null");
        }
        if (ruleName == null || ruleName.isBlank()) {
            throw new IllegalArgumentException("Rule name cannot be null or blank");
        }
    }
}
