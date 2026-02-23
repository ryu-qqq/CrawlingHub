package com.ryuqq.crawlinghub.adapter.out.http.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HTTP Client 공통 설정 Properties
 *
 * <p>WebClient의 타임아웃 및 버퍼 크기 설정을 관리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "http-client")
public class HttpClientProperties {

    /** 연결 타임아웃 (초) */
    private int connectTimeout = 10;

    /** 요청 타임아웃 (초) */
    private int requestTimeout = 30;

    /** 최대 응답 버퍼 크기 (bytes) - 기본 5MB */
    private int maxInMemorySize = 5 * 1024 * 1024;

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

    public int getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }
}
