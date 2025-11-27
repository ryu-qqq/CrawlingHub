package com.ryuqq.cralwinghub.domain.fixture.execution;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionResult;

/**
 * CrawlExecutionResult Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlExecutionResultFixture {

    private static final String SUCCESS_RESPONSE = "{\"status\":\"success\",\"data\":{}}";
    private static final String FAILURE_RESPONSE = "{\"error\":\"Something went wrong\"}";

    /**
     * 빈 결과 생성 (실행 중)
     *
     * @return CrawlExecutionResult (empty)
     */
    public static CrawlExecutionResult empty() {
        return CrawlExecutionResult.empty();
    }

    /**
     * 성공 결과 생성 (HTTP 200)
     *
     * @return CrawlExecutionResult (success)
     */
    public static CrawlExecutionResult aSuccessResult() {
        return CrawlExecutionResult.success(SUCCESS_RESPONSE, 200);
    }

    /**
     * 성공 결과 생성 (특정 응답)
     *
     * @param responseBody 응답 본문
     * @return CrawlExecutionResult (success)
     */
    public static CrawlExecutionResult aSuccessResult(String responseBody) {
        return CrawlExecutionResult.success(responseBody, 200);
    }

    /**
     * 실패 결과 생성 (HTTP 500)
     *
     * @return CrawlExecutionResult (failure)
     */
    public static CrawlExecutionResult aFailureResult() {
        return CrawlExecutionResult.failure(500, "Internal Server Error");
    }

    /**
     * 실패 결과 생성 (응답 본문 포함)
     *
     * @return CrawlExecutionResult (failure with body)
     */
    public static CrawlExecutionResult aFailureResultWithBody() {
        return CrawlExecutionResult.failureWithBody(FAILURE_RESPONSE, 400, "Bad Request");
    }

    /**
     * 타임아웃 결과 생성
     *
     * @return CrawlExecutionResult (timeout)
     */
    public static CrawlExecutionResult aTimeoutResult() {
        return CrawlExecutionResult.timeout("Request timed out after 30000ms");
    }

    /**
     * Rate Limit 결과 생성 (HTTP 429)
     *
     * @return CrawlExecutionResult (rate limited)
     */
    public static CrawlExecutionResult aRateLimitedResult() {
        return CrawlExecutionResult.failure(429, "Too Many Requests");
    }

    private CrawlExecutionResultFixture() {
        // Utility class
    }
}
