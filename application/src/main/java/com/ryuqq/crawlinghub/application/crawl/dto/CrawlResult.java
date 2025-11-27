package com.ryuqq.crawlinghub.application.crawl.dto;

/**
 * 크롤링 결과 DTO
 *
 * <p>크롤러 실행 결과를 담는 DTO. HTTP 응답 정보와 성공/실패 상태를 포함.
 *
 * <p><strong>DTO로 설계한 이유</strong>:
 *
 * <ul>
 *   <li>크롤링 실행 결과 데이터 전달 목적
 *   <li>도메인 규칙을 표현하지 않음 (VO 아님)
 *   <li>Application 레이어 내부에서만 사용
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlResult {

    private final boolean success;
    private final String responseBody;
    private final Integer httpStatusCode;
    private final String errorMessage;

    private CrawlResult(
            boolean success, String responseBody, Integer httpStatusCode, String errorMessage) {
        this.success = success;
        this.responseBody = responseBody;
        this.httpStatusCode = httpStatusCode;
        this.errorMessage = errorMessage;
    }

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
            return success(response.getBody(), response.getStatusCode());
        } else {
            String errorMessage = buildErrorMessage(response);
            return failure(response.getStatusCode(), errorMessage);
        }
    }

    /** HTTP 응답으로부터 에러 메시지 생성 */
    private static String buildErrorMessage(HttpResponse response) {
        if (response.isRateLimited()) {
            return "Rate limited (429)";
        } else if (response.isServerError()) {
            return "Server error: " + response.getStatusCode();
        } else if (response.isClientError()) {
            return "Client error: " + response.getStatusCode();
        } else {
            return "HTTP error: " + response.getStatusCode();
        }
    }

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
