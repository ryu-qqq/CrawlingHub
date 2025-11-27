package com.ryuqq.cralwinghub.domain.fixture.crawl.task;

import com.ryuqq.crawlinghub.domain.task.vo.RetryCount;

/**
 * RetryCount Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class RetryCountFixture {

    /**
     * 재시도 횟수 0 생성
     *
     * @return RetryCount (value = 0)
     */
    public static RetryCount zero() {
        return RetryCount.zero();
    }

    /**
     * 재시도 횟수 1 생성
     *
     * @return RetryCount (value = 1)
     */
    public static RetryCount one() {
        return new RetryCount(1);
    }

    /**
     * 재시도 횟수 2 생성 (최대값)
     *
     * @return RetryCount (value = 2)
     */
    public static RetryCount maxRetry() {
        return new RetryCount(RetryCount.MAX_RETRY_COUNT);
    }

    /**
     * 특정 값으로 재시도 횟수 생성
     *
     * @param value 재시도 횟수
     * @return RetryCount
     */
    public static RetryCount of(int value) {
        return new RetryCount(value);
    }

    private RetryCountFixture() {
        // Utility class
    }
}
