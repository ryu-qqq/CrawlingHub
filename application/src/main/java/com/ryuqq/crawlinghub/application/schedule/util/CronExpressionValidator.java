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
     * Converts standard cron expression to AWS EventBridge format
     * Standard: "0 9 * * *" (minute hour day month dayOfWeek)
     * AWS: "0 9 * * ? *" (minute hour day month dayOfWeek year)
     *
     * @param standardCron the standard cron expression
     * @return AWS-compatible cron expression
     */
    public static String convertToAwsCronExpression(String standardCron) {
        validate(standardCron);

        String[] parts = standardCron.trim().split("\\s+");
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
            // Already 6 fields, assume it's AWS format
            return standardCron;
        } else {
            throw new InvalidCronExpressionException("Unsupported cron format: " + standardCron);
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
