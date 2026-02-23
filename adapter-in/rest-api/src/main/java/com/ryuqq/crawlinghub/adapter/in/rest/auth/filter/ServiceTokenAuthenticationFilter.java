package com.ryuqq.crawlinghub.adapter.in.rest.auth.filter;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.config.ServiceTokenProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Service Token 기반 인증 필터
 *
 * <p>서버 간 내부 통신에서 X-Service-Token / X-Service-Name 헤더로 인증을 처리합니다.
 *
 * <p>인증 흐름:
 *
 * <ul>
 *   <li>{@code enabled=false}: anonymous 사용자에게 ROLE_SERVICE 부여 (로컬 개발 bypass)
 *   <li>{@code enabled=true} + 토큰 일치: X-Service-Name에 ROLE_SERVICE 부여
 *   <li>{@code enabled=true} + 토큰 불일치: SecurityContext 비워둠 → 401 응답
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class ServiceTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_SERVICE_TOKEN = "X-Service-Token";
    private static final String HEADER_SERVICE_NAME = "X-Service-Name";
    private static final String ROLE_SERVICE = "ROLE_SERVICE";

    private final ServiceTokenProperties properties;

    public ServiceTokenAuthenticationFilter(ServiceTokenProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!properties.enabled()) {
            setAuthentication("anonymous");
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(HEADER_SERVICE_TOKEN);
        String serviceName = request.getHeader(HEADER_SERVICE_NAME);

        if (properties.secret().equals(token)) {
            setAuthentication(serviceName != null ? serviceName : "unknown-service");
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String principal) {
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(ROLE_SERVICE));
        PreAuthenticatedAuthenticationToken authentication =
                new PreAuthenticatedAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
