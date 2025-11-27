package com.ryuqq.cralwinghub.domain.fixture.schedule;

import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;

/**
 * CronExpression Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CronExpressionFixture {

    // 매시 정각 실행 (기본)
    private static final String DEFAULT_CRON = "cron(0 * * * ? *)";

    // 매일 자정 실행
    private static final String DAILY_MIDNIGHT = "cron(0 0 * * ? *)";

    // 매주 월요일 오전 9시 실행
    private static final String WEEKLY_MONDAY = "cron(0 9 ? * MON *)";

    /**
     * 기본 Cron 표현식 (매시 정각)
     *
     * @return CronExpression
     */
    public static CronExpression aDefaultCron() {
        return CronExpression.of(DEFAULT_CRON);
    }

    /**
     * 매일 자정 Cron 표현식
     *
     * @return CronExpression
     */
    public static CronExpression aDailyMidnightCron() {
        return CronExpression.of(DAILY_MIDNIGHT);
    }

    /**
     * 매주 월요일 오전 9시 Cron 표현식
     *
     * @return CronExpression
     */
    public static CronExpression aWeeklyMondayCron() {
        return CronExpression.of(WEEKLY_MONDAY);
    }

    /**
     * 특정 값으로 Cron 표현식 생성
     *
     * @param value Cron 표현식 문자열
     * @return CronExpression
     */
    public static CronExpression aCron(String value) {
        return CronExpression.of(value);
    }

    private CronExpressionFixture() {
        // Utility class
    }
}
