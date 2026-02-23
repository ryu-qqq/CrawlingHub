package com.ryuqq.crawlinghub.adapter.in.rest.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Service Token 인증 설정
 *
 * <p>서버 간 내부 통신용 Service Token 기반 인증에 사용되는 설정입니다.
 *
 * <ul>
 *   <li>{@code enabled=false}: 로컬 개발 환경에서 인증 없이 접근 허용 (anonymous에 ROLE_SERVICE 부여)
 *   <li>{@code enabled=true}: X-Service-Token 헤더 검증 후 인증 처리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.service-token")
public record ServiceTokenProperties(boolean enabled, String secret) {
    public ServiceTokenProperties {
        if (secret == null) {
            secret = "";
        }
    }
}
