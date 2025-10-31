package com.ryuqq.crawlinghub.domain.crawl.schedule;

import java.util.Objects;

/**
 * Cron 표현식 Value Object
 *
 * <p>Pure Domain Object - 외부 라이브러리 의존성 없음</p>
 * <p>형식 검증은 Application Layer의 CronExpressionValidator에 위임</p>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public class CronExpression {

    private final String expression;

    private CronExpression(String expression) {
        validateNotNull(expression);
        this.expression = expression;
    }

    public static CronExpression of(String expression) {
        return new CronExpression(expression);
    }

    private static void validateNotNull(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Cron 표현식은 필수입니다");
        }
    }

    public String getValue() {
        return expression;
    }

    public boolean isSameAs(CronExpression other) {
        if (other == null) {
            return false;
        }
        return this.expression.equals(other.expression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CronExpression that = (CronExpression) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public String toString() {
        return expression;
    }
}
