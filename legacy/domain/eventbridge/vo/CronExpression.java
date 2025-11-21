package com.ryuqq.crawlinghub.domain.eventbridge.vo;

import com.ryuqq.crawlinghub.domain.eventbridge.exception.InvalidCronExpressionException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AWS EventBridge Cron Expression Value Object
 *
 * <p>형식: {@code cron(minute hour day-of-month month day-of-week year)}
 *
 * <p>최소 실행 간격: 1시간
 */
public record CronExpression(String value) {

    private static final Pattern AWS_CRON_PATTERN =
            Pattern.compile("^cron\\((?<body>.+)\\)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FIELD_SEPARATOR = Pattern.compile("\\s+");
    private static final Pattern FIELD_ALLOWED_PATTERN = Pattern.compile("^[A-Za-z0-9*/?,#LW-]+$");
    private static final Pattern MINUTE_FIELD_PATTERN = Pattern.compile("^(?:[0-9]|[0-5][0-9])$");
    private static final String BODY_GROUP = "body";
    private static final int AWS_CRON_FIELD_COUNT = 6;

    public CronExpression {
        String normalized = normalize(value);
        String[] fields = validateAwsEventBridgeFormat(normalized);
        validateMinimumInterval(normalized, fields[0]);
        value = normalized;
    }

    public static CronExpression of(String value) {
        return new CronExpression(value);
    }

    private static String normalize(String expression) {
        if (expression == null) {
            throw InvalidCronExpressionException.dueToInvalidFormat(null);
        }
        String normalized = expression.trim();
        if (normalized.isEmpty()) {
            throw InvalidCronExpressionException.dueToInvalidFormat(expression);
        }
        return normalized;
    }

    private static String[] validateAwsEventBridgeFormat(String expression) {
        Matcher matcher = AWS_CRON_PATTERN.matcher(expression);
        if (!matcher.matches()) {
            throw InvalidCronExpressionException.dueToInvalidFormat(expression);
        }

        String body = matcher.group(BODY_GROUP).trim();
        String[] fields = FIELD_SEPARATOR.split(body);

        if (fields.length != AWS_CRON_FIELD_COUNT) {
            throw InvalidCronExpressionException.dueToInvalidFormat(expression);
        }

        boolean hasInvalidField =
                Arrays.stream(fields)
                        .anyMatch(
                                field ->
                                        field.isBlank()
                                                || !FIELD_ALLOWED_PATTERN.matcher(field).matches());

        if (hasInvalidField) {
            throw InvalidCronExpressionException.dueToInvalidFormat(expression);
        }

        return fields;
    }

    private static void validateMinimumInterval(String expression, String minuteField) {
        if (!MINUTE_FIELD_PATTERN.matcher(minuteField).matches()) {
            throw InvalidCronExpressionException.dueToInsufficientInterval(expression);
        }
    }
}
