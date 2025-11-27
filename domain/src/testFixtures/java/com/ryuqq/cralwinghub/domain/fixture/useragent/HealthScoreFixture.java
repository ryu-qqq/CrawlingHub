package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;

/**
 * HealthScore Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class HealthScoreFixture {

    /**
     * 초기 Health Score (100)
     *
     * @return HealthScore (100)
     */
    public static HealthScore initial() {
        return HealthScore.initial();
    }

    /**
     * 복구 Health Score (70)
     *
     * @return HealthScore (70)
     */
    public static HealthScore recovered() {
        return HealthScore.recovered();
    }

    /**
     * 정지 임계값 근처 Health Score (29)
     *
     * @return HealthScore (29)
     */
    public static HealthScore belowThreshold() {
        return HealthScore.of(29);
    }

    /**
     * 정지 임계값 Health Score (30)
     *
     * @return HealthScore (30)
     */
    public static HealthScore atThreshold() {
        return HealthScore.of(30);
    }

    /**
     * 최소 Health Score (0)
     *
     * @return HealthScore (0)
     */
    public static HealthScore minimum() {
        return HealthScore.of(0);
    }

    /**
     * 특정 값으로 Health Score 생성
     *
     * @param value Health Score 값
     * @return HealthScore
     */
    public static HealthScore of(int value) {
        return HealthScore.of(value);
    }

    private HealthScoreFixture() {
        // Utility class
    }
}
