package com.ryuqq.crawlinghub.application.schedule.util;

import com.ryuqq.crawlinghub.application.schedule.usecase.InvalidCronExpressionException;
import org.springframework.scheduling.support.CronExpression;

/**
 * Utility class for validating and converting cron expressions
 */
public class CronExpressionValidator {

    private CronExpressionValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Validates a standard cron expression
     *
     * @param cronExpression the cron expression to validate (e.g., "0 9 * * *")
     * @throws InvalidCronExpressionException if the expression is invalid
     */
    public static void validate(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new InvalidCronExpressionException("Cron expression cannot be null or blank");
        }

        try {
            CronExpression.parse(cronExpression);
        } catch (IllegalArgumentException e) {
            throw new InvalidCronExpressionException(cronExpression, e);
        }
    }

    /**
     * Converts Spring cron expression (6-field) to AWS EventBridge format
     * Spring: "0 0 9 * * *" (second minute hour day month dayOfWeek)
     * AWS: "0 9 * * ? *" (minute hour day month dayOfWeek year)
     *
     * @param springCron the Spring cron expression (6-field)
     * @return AWS-compatible cron expression (6-field)
     */
    public static String convertToAwsCronExpression(String springCron) {
        validate(springCron);

        String[] parts = springCron.trim().split("\\s+");
        if (parts.length == 5) {
            // Standard 5-field cron: minute hour day month dayOfWeek
            // AWS requires 6 fields: minute hour day month dayOfWeek year
            // Replace day-of-week with ? if day-of-month is specified (not *)
            String dayOfMonth = parts[2];
            String dayOfWeek = parts[4];

            if (!"*".equals(dayOfMonth) && !"?".equals(dayOfMonth)) {
                // If day-of-month is specified, day-of-week must be ?
                return String.format("%s %s %s %s ? *", parts[0], parts[1], parts[2], parts[3]);
            } else if (!"*".equals(dayOfWeek) && !"?".equals(dayOfWeek)) {
                // If day-of-week is specified, day-of-month must be ?
                return String.format("%s %s ? %s %s *", parts[0], parts[1], parts[3], parts[4]);
            } else {
                // Both are *, use standard conversion
                return String.format("%s %s %s %s ? *", parts[0], parts[1], parts[2], parts[3]);
            }
        } else if (parts.length == 6) {
            // Spring 6-field cron: second minute hour day month dayOfWeek
            // AWS 6-field cron: minute hour day month dayOfWeek year
            // Need to remove second field and add year field

            String second = parts[0];
            if (!"0".equals(second)) {
                throw new InvalidCronExpressionException(
                    "AWS EventBridge does not support non-zero seconds. Use 0 as the first field. Got: " + second
                );
            }

            String minute = parts[1];
            String hour = parts[2];
            String dayOfMonth = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];

            // AWS requires either day or dayOfWeek to be '?'
            if (!"*".equals(dayOfMonth) && !"?".equals(dayOfMonth)) {
                // If day-of-month is specified, day-of-week must be ?
                return String.format("%s %s %s %s ? *", minute, hour, dayOfMonth, month);
            } else if (!"*".equals(dayOfWeek) && !"?".equals(dayOfWeek)) {
                // If day-of-week is specified, day-of-month must be ?
                return String.format("%s %s ? %s %s *", minute, hour, month, dayOfWeek);
            } else {
                // Both are *, default to day-of-month and set day-of-week to ?
                return String.format("%s %s %s %s ? *", minute, hour, dayOfMonth, month);
            }
        } else {
            throw new InvalidCronExpressionException("Unsupported cron format: " + springCron);
        }
    }

    /**
     * Checks if the cron expression is valid
     *
     * @param cronExpression the cron expression to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String cronExpression) {
        try {
            validate(cronExpression);
            return true;
        } catch (InvalidCronExpressionException e) {
            return false;
        }
    }
}
