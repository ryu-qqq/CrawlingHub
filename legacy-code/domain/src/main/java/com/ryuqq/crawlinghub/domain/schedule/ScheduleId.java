package com.ryuqq.crawlinghub.domain.schedule;

/**
 * Value object representing a schedule identifier
 * Ensures type safety and validation for schedule IDs
 *
 * @param value The numeric ID value (must be positive)
 * @author crawlinghub (noreply@crawlinghub.com)
 */
public record ScheduleId(Long value) {

    /**
     * Compact constructor with validation.
     */
    public ScheduleId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Schedule ID must be positive: " + value);
        }
    }

    /**
     * Factory method to create a ScheduleId from a Long value.
     *
     * @param value The numeric ID value
     * @return A new ScheduleId instance
     */
    public static ScheduleId of(Long value) {
        return new ScheduleId(value);
    }

}
