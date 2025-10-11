package com.ryuqq.crawlinghub.domain.schedule;

public record ScheduleId(Long value) {

    public ScheduleId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Schedule ID must be positive: " + value);
        }
    }

    public static ScheduleId of(Long value) {
        return new ScheduleId(value);
    }

}
