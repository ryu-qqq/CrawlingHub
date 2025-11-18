package com.ryuqq.crawlinghub.domain.fixture.eventbridge;

import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;

/**
 * CronExpression 테스트 픽스처
 *
 * <p>AWS EventBridge Cron Expression 값들을 중앙에서 관리하여 테스트 중복을 제거합니다.</p>
 */
public final class CronExpressionFixture {

    private static final String DEFAULT_EXPRESSION = "cron(0 12 * * ? *)";

    private CronExpressionFixture() {
    }

    public static CronExpression forNew() {
        return CronExpression.of(DEFAULT_EXPRESSION);
    }

    public static CronExpression of() {
        return CronExpression.of(DEFAULT_EXPRESSION);
    }

    public static CronExpression reconstitute() {
        return CronExpression.of(DEFAULT_EXPRESSION);
    }

    public static CronExpression aCronExpression() {
        return CronExpression.of(DEFAULT_EXPRESSION);
    }

    public static String aValidCronExpressionValue() {
        return DEFAULT_EXPRESSION;
    }

    public static String anInvalidCronExpressionValue() {
        return "0 12 * * ? *";
    }

    public static String anExpressionWithLessThanOneHourInterval() {
        return "cron(0/30 * * * ? *)";
    }

    public static String anInvalidCronExpression() {
        return anInvalidCronExpressionValue();
    }
}

