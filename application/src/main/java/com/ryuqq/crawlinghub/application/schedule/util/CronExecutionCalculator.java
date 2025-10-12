package com.ryuqq.crawlinghub.application.schedule.util;

import com.ryuqq.crawlinghub.application.schedule.usecase.InvalidCronExpressionException;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for calculating cron execution times
 */
public class CronExecutionCalculator {

    private CronExecutionCalculator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Calculates the next execution time for a cron expression
     *
     * @param cronExpression the cron expression
     * @param timezone the timezone (e.g., "Asia/Seoul")
     * @return the next execution time in LocalDateTime
     * @throws InvalidCronExpressionException if the expression is invalid
     */
    public static LocalDateTime calculateNextExecution(String cronExpression, String timezone) {
        CronExpressionValidator.validate(cronExpression);

        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            ZonedDateTime next = cron.next(now);

            if (next == null) {
                throw new InvalidCronExpressionException("Unable to calculate next execution time for: " + cronExpression);
            }

            return next.toLocalDateTime();
        } catch (IllegalArgumentException e) {
            throw new InvalidCronExpressionException(cronExpression, e);
        }
    }

    /**
     * Calculates the next execution time from a specific base time
     *
     * @param cronExpression the cron expression
     * @param timezone the timezone
     * @param fromDateTime the base time to calculate from
     * @return the next execution time in LocalDateTime
     */
    public static LocalDateTime calculateNextExecutionFrom(String cronExpression, String timezone, LocalDateTime fromDateTime) {
        CronExpressionValidator.validate(cronExpression);

        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime from = fromDateTime.atZone(zoneId);
            ZonedDateTime next = cron.next(from);

            if (next == null) {
                throw new InvalidCronExpressionException("Unable to calculate next execution time for: " + cronExpression);
            }

            return next.toLocalDateTime();
        } catch (IllegalArgumentException e) {
            throw new InvalidCronExpressionException(cronExpression, e);
        }
    }

    /**
     * Calculates multiple next execution times
     *
     * @param cronExpression the cron expression
     * @param timezone the timezone
     * @param count the number of next executions to calculate
     * @return array of next execution times
     */
    public static LocalDateTime[] calculateNextExecutions(String cronExpression, String timezone, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }

        LocalDateTime[] executions = new LocalDateTime[count];
        LocalDateTime current = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            current = calculateNextExecutionFrom(cronExpression, timezone, current);
            executions[i] = current;
        }

        return executions;
    }
}
