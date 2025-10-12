package com.ryuqq.crawlinghub.application.schedule.usecase;

public class InvalidScheduleException extends RuntimeException {

    public InvalidScheduleException(String message) {
        super(message);
    }

    public InvalidScheduleException(String message, Throwable cause) {
        super(message, cause);
    }
}
