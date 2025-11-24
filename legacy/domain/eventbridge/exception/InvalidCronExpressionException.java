package com.ryuqq.crawlinghub.domain.eventbridge.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;
import java.util.Map;

/**
 * Cron Expression 검증 실패 예외
 *
 * <p>AWS EventBridge 형식 위반 또는 최소 실행 간격 위반 시 발생합니다.
 */
public final class InvalidCronExpressionException extends DomainException {

    private static final String ERROR_CODE = "EVENTBRIDGE-CRON-001";
    private final String expression;

    private InvalidCronExpressionException(String message, String expression) {
        super(message);
        this.expression = expression;
    }

    public static InvalidCronExpressionException dueToInvalidFormat(String expression) {
        String message =
                String.format(
                        "Cron expression must follow AWS EventBridge format 'cron(minute hour"
                                + " day-of-month month day-of-week year)': %s",
                        expression);
        return new InvalidCronExpressionException(message, expression);
    }

    public static InvalidCronExpressionException dueToInsufficientInterval(String expression) {
        String message =
                String.format(
                        "Cron expression must execute at intervals of at least 1 hour: %s",
                        expression);
        return new InvalidCronExpressionException(message, expression);
    }

    @Override
    public String code() {
        return ERROR_CODE;
    }

    @Override
    public Map<String, Object> args() {
        return Map.of("expression", expression);
    }
}
