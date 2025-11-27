package com.ryuqq.crawlinghub.application.crawl.dto;

import java.util.Map;

/**
 * HTTP 응답 DTO
 *
 * <p>크롤링 HTTP 응답 정보를 담는 DTO.
 *
 * @author development-team
 * @since 1.0.0
 */
public class HttpResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    private HttpResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    /**
     * 성공 응답 생성
     *
     * @param statusCode HTTP 상태 코드
     * @param body 응답 바디
     * @return HttpResponse
     */
    public static HttpResponse of(int statusCode, String body) {
        return new HttpResponse(statusCode, body, null);
    }

    /**
     * 성공 응답 생성 (헤더 포함)
     *
     * @param statusCode HTTP 상태 코드
     * @param body 응답 바디
     * @param headers 응답 헤더
     * @return HttpResponse
     */
    public static HttpResponse of(int statusCode, String body, Map<String, String> headers) {
        return new HttpResponse(statusCode, body, headers);
    }

    /**
     * 성공 여부 확인 (2xx)
     *
     * @return 2xx 상태 코드이면 true
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * 클라이언트 에러 여부 확인 (4xx)
     *
     * @return 4xx 상태 코드이면 true
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * 서버 에러 여부 확인 (5xx)
     *
     * @return 5xx 상태 코드이면 true
     */
    public boolean isServerError() {
        return statusCode >= 500;
    }

    /**
     * Rate Limit 에러 여부 확인 (429)
     *
     * @return 429 상태 코드이면 true
     */
    public boolean isRateLimited() {
        return statusCode == 429;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
