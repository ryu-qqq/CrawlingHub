package com.ryuqq.cralwinghub.domain.fixture.schedule;

import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;

/**
 * SchedulerName Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class SchedulerNameFixture {

    private static final String DEFAULT_NAME = "test-scheduler";

    /**
     * 기본 스케줄러명 생성
     *
     * @return SchedulerName
     */
    public static SchedulerName aDefaultName() {
        return SchedulerName.of(DEFAULT_NAME);
    }

    /**
     * 특정 값으로 스케줄러명 생성
     *
     * @param value 스케줄러명
     * @return SchedulerName
     */
    public static SchedulerName aName(String value) {
        return SchedulerName.of(value);
    }

    private SchedulerNameFixture() {
        // Utility class
    }
}
