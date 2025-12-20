package com.ryuqq.crawlinghub.adapter.out.http.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HTTP Client 설정 Properties
 *
 * <p>세션 토큰 발급을 위한 HTTP Client 설정을 관리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "http-client.session")
public class HttpClientProperties {

    /** 세션 토큰 발급 대상 URL */
    private String targetUrl = "https://m.web.mustit.co.kr/";

    /** 세션 쿠키 이름 */
    private String sessionCookieName = "PHPSESSID";

    /** 연결 타임아웃 (초) */
    private int connectTimeout = 10;

    /** 요청 타임아웃 (초) */
    private int requestTimeout = 30;

    /** 기본 세션 유지 시간 (시간, expires가 없을 때) */
    private int defaultSessionDurationHours = 2;

    /** 최대 응답 버퍼 크기 (bytes) - 기본 5MB */
    private int maxInMemorySize = 5 * 1024 * 1024;

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getSessionCookieName() {
        return sessionCookieName;
    }

    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getDefaultSessionDurationHours() {
        return defaultSessionDurationHours;
    }

    public void setDefaultSessionDurationHours(int defaultSessionDurationHours) {
        this.defaultSessionDurationHours = defaultSessionDurationHours;
    }

    public int getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }
}
