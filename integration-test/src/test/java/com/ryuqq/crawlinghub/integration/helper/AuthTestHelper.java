package com.ryuqq.crawlinghub.integration.helper;

import org.springframework.http.HttpHeaders;

/**
 * 통합 테스트용 인증 헬퍼
 *
 * <p>Service Token 인증 헤더를 생성하여 테스트에서 인증된 요청을 시뮬레이션합니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * HttpHeaders headers = AuthTestHelper.serviceAuth();
 * restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), responseType);
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class AuthTestHelper {

    public static final String HEADER_SERVICE_TOKEN = "X-Service-Token";
    public static final String HEADER_SERVICE_NAME = "X-Service-Name";
    public static final String DEFAULT_SERVICE_TOKEN = "test-service-token";
    public static final String DEFAULT_SERVICE_NAME = "test-service";

    private AuthTestHelper() {}

    /**
     * Service Token 인증 헤더 생성
     *
     * @return Service Token 인증 헤더
     */
    public static HttpHeaders serviceAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_SERVICE_TOKEN, DEFAULT_SERVICE_TOKEN);
        headers.set(HEADER_SERVICE_NAME, DEFAULT_SERVICE_NAME);
        return headers;
    }

    /**
     * 인증되지 않은 요청을 위한 빈 헤더
     *
     * @return 빈 헤더 (인증 없음)
     */
    public static HttpHeaders unauthenticated() {
        return new HttpHeaders();
    }
}
