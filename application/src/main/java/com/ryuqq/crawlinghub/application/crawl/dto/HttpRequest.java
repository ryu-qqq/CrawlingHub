package com.ryuqq.crawlinghub.application.crawl.dto;

import java.util.Map;

/**
 * HTTP 요청 DTO (Record)
 *
 * <p>크롤링을 위한 HTTP 요청 정보를 담는 불변 DTO.
 *
 * @param url 요청 URL
 * @param headers HTTP 헤더 맵
 * @param body 요청 바디 (POST 요청 시)
 * @author development-team
 * @since 1.0.0
 */
public record HttpRequest(String url, Map<String, String> headers, String body) {

    /** Compact constructor - headers를 불변 맵으로 변환 */
    public HttpRequest {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    /**
     * GET 요청 생성
     *
     * @param url 요청 URL
     * @return HttpRequest
     */
    public static HttpRequest get(String url) {
        return new HttpRequest(url, null, null);
    }

    /**
     * GET 요청 생성 (헤더 포함)
     *
     * @param url 요청 URL
     * @param headers 헤더 맵
     * @return HttpRequest
     */
    public static HttpRequest get(String url, Map<String, String> headers) {
        return new HttpRequest(url, headers, null);
    }

    /**
     * POST 요청 생성
     *
     * @param url 요청 URL
     * @param body 요청 바디
     * @return HttpRequest
     */
    public static HttpRequest post(String url, String body) {
        return new HttpRequest(url, null, body);
    }

    /**
     * POST 요청 생성 (헤더 포함)
     *
     * @param url 요청 URL
     * @param headers 헤더 맵
     * @param body 요청 바디
     * @return HttpRequest
     */
    public static HttpRequest post(String url, Map<String, String> headers, String body) {
        return new HttpRequest(url, headers, body);
    }

    // === Accessor aliases for backward compatibility ===

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
