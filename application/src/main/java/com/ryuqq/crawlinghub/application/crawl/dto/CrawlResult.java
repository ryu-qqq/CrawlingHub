package com.ryuqq.crawlinghub.application.crawl.dto;

/**
 * 크롤링 결과 DTO (Record)
 *
 * <p>크롤러 실행 결과를 담는 불변 DTO. HTTP 응답 정보와 성공/실패 상태를 포함.
 *
 * @param success 성공 여부
 * @param responseBody HTTP 응답 바디
 * @param httpStatusCode HTTP 상태 코드
 * @param errorMessage 에러 메시지 (실패 시)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlResult(
        boolean success, String responseBody, Integer httpStatusCode, String errorMessage) {

    /**
     * 성공 결과 생성
     *
     * @param responseBody HTTP 응답 바디
     * @param httpStatusCode HTTP 상태 코드
     * @return 성공 결과
     */
    public static CrawlResult success(String responseBody, int httpStatusCode) {
        return new CrawlResult(true, responseBody, httpStatusCode, null);
    }

    /**
     * 실패 결과 생성
     *
     * @param errorMessage 에러 메시지
     * @return 실패 결과
     */
    public static CrawlResult failure(String errorMessage) {
        return new CrawlResult(false, null, null, errorMessage);
    }

    /**
     * HTTP 에러로 인한 실패 결과 생성
     *
     * @param httpStatusCode HTTP 상태 코드
     * @param errorMessage 에러 메시지
     * @return 실패 결과
     */
    public static CrawlResult failure(int httpStatusCode, String errorMessage) {
        return new CrawlResult(false, null, httpStatusCode, errorMessage);
    }

    /**
     * HttpResponse로부터 CrawlResult 생성
     *
     * <p>HTTP 응답의 성공/실패 여부에 따라 적절한 결과를 생성합니다.
     *
     * @param response HTTP 응답
     * @return CrawlResult (성공 또는 실패)
     */
    public static CrawlResult from(HttpResponse response) {
        if (response.isSuccess()) {
            return success(response.body(), response.statusCode());
        } else {
            String errorMessage = buildErrorMessage(response);
            return failure(response.statusCode(), errorMessage);
        }
    }

    /** HTTP 응답으로부터 에러 메시지 생성 */
    private static String buildErrorMessage(HttpResponse response) {
        if (response.isRateLimited()) {
            return "Rate limited (429)";
        } else if (response.isServerError()) {
            return "Server error: " + response.statusCode();
        } else if (response.isClientError()) {
            return "Client error: " + response.statusCode();
        } else {
            return "HTTP error: " + response.statusCode();
        }
    }

    // === Accessor aliases for backward compatibility ===

    public boolean isSuccess() {
        return success;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
