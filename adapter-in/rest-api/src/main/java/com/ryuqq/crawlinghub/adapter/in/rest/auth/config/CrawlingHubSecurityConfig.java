package com.ryuqq.crawlinghub.adapter.in.rest.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.auth.filter.ServiceTokenAuthenticationFilter;
import com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * CrawlingHub Security 설정
 *
 * <p>Service Token 기반 Stateless 인증을 구성합니다. 서버 간 내부 통신 전용이므로 CORS, @PreAuthorize 등 불필요합니다.
 *
 * <p>인증 흐름:
 *
 * <pre>
 * 서비스 → X-Service-Token + X-Service-Name 헤더 → ServiceTokenAuthenticationFilter
 *                                                → SecurityContext (ROLE_SERVICE)
 *                                                → anyRequest().authenticated()
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ServiceTokenProperties.class)
public class CrawlingHubSecurityConfig {

    private final ServiceTokenProperties serviceTokenProperties;
    private final ObjectMapper objectMapper;

    public CrawlingHubSecurityConfig(
            ServiceTokenProperties serviceTokenProperties, ObjectMapper objectMapper) {
        this.serviceTokenProperties = serviceTokenProperties;
        this.objectMapper = objectMapper;
    }

    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        exception ->
                                exception
                                        .authenticationEntryPoint(
                                                (request, response, authException) ->
                                                        writeErrorResponse(
                                                                request,
                                                                response,
                                                                HttpStatus.UNAUTHORIZED,
                                                                "UNAUTHORIZED",
                                                                "인증이 필요합니다."))
                                        .accessDeniedHandler(
                                                (request, response, accessDeniedException) ->
                                                        writeErrorResponse(
                                                                request,
                                                                response,
                                                                HttpStatus.FORBIDDEN,
                                                                "FORBIDDEN",
                                                                "접근 권한이 없습니다.")))
                .authorizeHttpRequests(
                        auth -> {
                            auth.requestMatchers(
                                            "/actuator/**", "/api/v1/health", "/api/v1/webhook/**")
                                    .permitAll();
                            auth.requestMatchers(
                                            "/api/v1/crawling/api-docs",
                                            "/api/v1/crawling/api-docs/**",
                                            "/api/v1/crawling/swagger",
                                            "/api/v1/crawling/swagger-ui/**",
                                            "/api/v1/crawling/docs/**",
                                            "/v3/api-docs/**",
                                            "/swagger-ui/**")
                                    .permitAll();
                            auth.anyRequest().authenticated();
                        })
                .addFilterBefore(
                        new ServiceTokenAuthenticationFilter(serviceTokenProperties),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String detail)
            throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("x-error-code", code);

        Map<String, Object> problemDetail = new LinkedHashMap<>();
        problemDetail.put("type", "about:blank");
        problemDetail.put("title", status.getReasonPhrase());
        problemDetail.put("status", status.value());
        problemDetail.put("detail", detail);
        problemDetail.put("code", code);
        problemDetail.put("timestamp", DateTimeFormatUtils.nowIso8601());

        String uri = request.getRequestURI();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            uri = uri + "?" + request.getQueryString();
        }
        problemDetail.put("instance", uri);

        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
