package com.ryuqq.crawlinghub.domain.execution.vo;

/**
 * CrawlExecution 결과 Value Object
 *
 * <p>크롤링 실행의 결과 데이터를 캡슐화합니다.
 *
 * <p><strong>성공 시</strong>:
 *
 * <ul>
 *   <li>responseBody: 크롤링 결과 데이터 (JSON 등)
 *   <li>httpStatusCode: 200
 *   <li>errorMessage: null
 * </ul>
 *
 * <p><strong>실패 시</strong>:
 *
 * <ul>
 *   <li>responseBody: null 또는 에러 응답
 *   <li>httpStatusCode: 4xx/5xx
 *   <li>errorMessage: 에러 메시지
 * </ul>
 *
 * @param responseBody 크롤링 응답 본문 (nullable, 대용량 가능)
 * @param httpStatusCode HTTP 상태 코드
 * @param errorMessage 에러 메시지 (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlExecutionResult(
        String responseBody, Integer httpStatusCode, String errorMessage) {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

    public CrawlExecutionResult {
        errorMessage = truncate(errorMessage, ERROR_MESSAGE_MAX_LENGTH);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * 성공 결과 생성
     *
     * @param responseBody 응답 본문
     * @param httpStatusCode HTTP 상태 코드 (2xx)
     * @return 성공 결과
     */
    public static CrawlExecutionResult success(String responseBody, Integer httpStatusCode) {
        return new CrawlExecutionResult(responseBody, httpStatusCode, null);
    }

    /**
     * 실패 결과 생성
     *
     * @param httpStatusCode HTTP 상태 코드 (4xx/5xx)
     * @param errorMessage 에러 메시지
     * @return 실패 결과
     */
    public static CrawlExecutionResult failure(Integer httpStatusCode, String errorMessage) {
        return new CrawlExecutionResult(null, httpStatusCode, errorMessage);
    }

    /**
     * 실패 결과 생성 (응답 본문 포함)
     *
     * @param responseBody 에러 응답 본문
     * @param httpStatusCode HTTP 상태 코드 (4xx/5xx)
     * @param errorMessage 에러 메시지
     * @return 실패 결과
     */
    public static CrawlExecutionResult failureWithBody(
            String responseBody, Integer httpStatusCode, String errorMessage) {
        return new CrawlExecutionResult(responseBody, httpStatusCode, errorMessage);
    }

    /**
     * 타임아웃 결과 생성
     *
     * @param errorMessage 타임아웃 에러 메시지
     * @return 타임아웃 결과
     */
    public static CrawlExecutionResult timeout(String errorMessage) {
        return new CrawlExecutionResult(null, null, errorMessage);
    }

    /**
     * 초기 상태 (실행 중) 생성
     *
     * @return 빈 결과
     */
    public static CrawlExecutionResult empty() {
        return new CrawlExecutionResult(null, null, null);
    }

    /**
     * 기존 데이터로 복원 (영속성 계층 전용)
     *
     * @param responseBody 응답 본문
     * @param httpStatusCode HTTP 상태 코드
     * @param errorMessage 에러 메시지
     * @return 복원된 CrawlExecutionResult
     */
    public static CrawlExecutionResult of(
            String responseBody, Integer httpStatusCode, String errorMessage) {
        return new CrawlExecutionResult(responseBody, httpStatusCode, errorMessage);
    }

    /**
     * 성공 여부 확인
     *
     * @return HTTP 2xx면 true
     */
    public boolean isSuccess() {
        return httpStatusCode != null && httpStatusCode >= 200 && httpStatusCode < 300;
    }

    /**
     * 클라이언트 에러 여부 확인
     *
     * @return HTTP 4xx면 true
     */
    public boolean isClientError() {
        return httpStatusCode != null && httpStatusCode >= 400 && httpStatusCode < 500;
    }

    /**
     * 서버 에러 여부 확인
     *
     * @return HTTP 5xx면 true
     */
    public boolean isServerError() {
        return httpStatusCode != null && httpStatusCode >= 500;
    }

    /**
     * Rate Limit 에러 여부 확인
     *
     * @return HTTP 429면 true
     */
    public boolean isRateLimited() {
        return httpStatusCode != null && httpStatusCode == 429;
    }

    /**
     * 에러 여부 확인
     *
     * @return errorMessage가 있으면 true
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }
}
