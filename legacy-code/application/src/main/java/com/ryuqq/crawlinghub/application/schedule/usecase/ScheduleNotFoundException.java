package com.ryuqq.crawlinghub.application.schedule.usecase;

public class ScheduleNotFoundException extends RuntimeException {

    public ScheduleNotFoundException(String message) {
        super(message);
    }

    public ScheduleNotFoundException(Long scheduleId) {
        super("Schedule not found with ID: " + scheduleId);
    }
}
