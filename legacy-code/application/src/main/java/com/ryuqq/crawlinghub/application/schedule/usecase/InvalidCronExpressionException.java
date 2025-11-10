package com.ryuqq.crawlinghub.application.schedule.usecase;

public class InvalidCronExpressionException extends RuntimeException {

    public InvalidCronExpressionException(String message) {
        super(message);
    }

    public InvalidCronExpressionException(String cronExpression, Throwable cause) {
        super("Invalid cron expression: " + cronExpression, cause);
    }
}
