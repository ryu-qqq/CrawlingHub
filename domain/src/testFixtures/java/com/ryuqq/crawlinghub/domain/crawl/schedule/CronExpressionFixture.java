package com.ryuqq.crawlinghub.domain.crawl.schedule;

import com.ryuqq.crawlinghub.domain.schedule.CronExpression;

/**
 * CronExpression Test Fixture
 *
 * <p>테스트에서 CronExpression 객체를 쉽게 생성하기 위한 Factory 클래스</p>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public class CronExpressionFixture {

    private static final String DEFAULT_EXPRESSION = "0 0 * * * *"; // 매시간
    private static final String HOURLY_EXPRESSION = "0 0 * * * *"; // 매시간
    private static final String DAILY_EXPRESSION = "0 0 0 * * *"; // 매일 자정
    private static final String EVERY_6_HOURS = "0 0 */6 * * *"; // 6시간마다

    /**
     * 기본 CronExpression 생성 (매시간)
     *
     * @return CronExpression
     */
    public static CronExpression create() {
        return CronExpression.of(DEFAULT_EXPRESSION);
    }

    /**
     * 지정된 표현식으로 CronExpression 생성
     *
     * @param expression Cron 표현식
     * @return CronExpression
     */
    public static CronExpression createWithExpression(String expression) {
        return CronExpression.of(expression);
    }

    /**
     * 매시간 실행 CronExpression 생성
     *
     * @return CronExpression
     */
    public static CronExpression createHourly() {
        return CronExpression.of(HOURLY_EXPRESSION);
    }

    /**
     * 매일 자정 실행 CronExpression 생성
     *
     * @return CronExpression
     */
    public static CronExpression createDaily() {
        return CronExpression.of(DAILY_EXPRESSION);
    }

    /**
     * 6시간마다 실행 CronExpression 생성
     *
     * @return CronExpression
     */
    public static CronExpression createEvery6Hours() {
        return CronExpression.of(EVERY_6_HOURS);
    }
}
