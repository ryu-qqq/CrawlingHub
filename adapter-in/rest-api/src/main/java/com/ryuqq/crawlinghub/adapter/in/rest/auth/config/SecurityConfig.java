package com.ryuqq.crawlinghub.adapter.in.rest.auth.config;

import com.ryuqq.authhub.sdk.filter.GatewayAuthenticationFilter;
import com.ryuqq.crawlinghub.adapter.in.rest.auth.filter.GatewaySecurityBridgeFilter;
import com.ryuqq.crawlinghub.adapter.in.rest.auth.handler.SecurityExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 설정
 *
 * <p>Gateway 연동 기반의 Stateless 인증을 구성합니다. Gateway에서 JWT 검증 후 X-* 헤더로 인증 정보를 전달합니다.
 *
 * <p>인증/인가 흐름:
 *
 * <pre>
 * Gateway (JWT 검증) → X-* 헤더 → GatewayAuthenticationFilter (AuthHub SDK) → UserContext
 *                                                                              ↓
 *                                                              @PreAuthorize (권한 기반 접근 제어)
 * </pre>
 *
 * <p>엔드포인트 권한 분류:
 *
 * <ul>
 *   <li>PUBLIC: 인증 불필요 (헬스체크, 웹훅)
 *   <li>DOCS: 인증 불필요 (API 문서, Swagger)
 *   <li>AUTHENTICATED: 인증 필요 + @PreAuthorize 권한 검사 (관리 API)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final SecurityExceptionHandler securityExceptionHandler;

    @Autowired
    public SecurityConfig(
            CorsProperties corsProperties, SecurityExceptionHandler securityExceptionHandler) {
        this.corsProperties = corsProperties;
        this.securityExceptionHandler = securityExceptionHandler;
    }

    /**
     * AuthHub SDK의 GatewayAuthenticationFilter Bean 등록
     *
     * <p>SDK의 AutoConfiguration에서는 이 필터를 자동 등록하지 않으므로 직접 Bean으로 생성합니다.
     */
    @Bean
    public GatewayAuthenticationFilter gatewayAuthenticationFilter() {
        return new GatewayAuthenticationFilter();
    }

    /**
     * UserContextHolder → SecurityContextHolder 브릿지 필터 Bean 등록
     *
     * <p>GatewayAuthenticationFilter가 설정한 UserContext를 Spring Security의 SecurityContextHolder에 연동하여
     * URL 기반 접근 제어와 @PreAuthorize 어노테이션이 모두 정상 동작하도록 합니다.
     */
    @Bean
    public GatewaySecurityBridgeFilter gatewaySecurityBridgeFilter() {
        return new GatewaySecurityBridgeFilter();
    }

    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 비활성화 (Stateless JWT 사용)
        http.csrf(AbstractHttpConfigurer::disable)
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션 비활성화 (Stateless)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 인증 실패 / 접근 거부 핸들러 설정
                .exceptionHandling(
                        exception ->
                                exception
                                        .authenticationEntryPoint(securityExceptionHandler)
                                        .accessDeniedHandler(securityExceptionHandler))
                // 엔드포인트 권한 설정
                .authorizeHttpRequests(this::configureAuthorization)
                // AuthHub SDK Gateway 인증 필터 추가 (X-* 헤더 기반)
                .addFilterBefore(
                        gatewayAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // UserContextHolder → SecurityContextHolder 브릿지 필터 (GatewayAuthenticationFilter 뒤에
                // 실행)
                .addFilterAfter(gatewaySecurityBridgeFilter(), GatewayAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 엔드포인트 권한 설정
     *
     * <p>관리 API의 세부 권한은 @PreAuthorize 어노테이션으로 처리됩니다.
     */
    private void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
                    auth) {

        // PUBLIC (인증 불필요)
        auth.requestMatchers("/actuator/**", "/api/v1/health", "/api/v1/webhook/**").permitAll();

        // API 문서 (인증 불필요)
        auth.requestMatchers(
                        "/api/v1/crawling/api-docs",
                        "/api/v1/crawling/api-docs/**",
                        "/api/v1/crawling/swagger",
                        "/api/v1/crawling/swagger-ui/**",
                        "/api/v1/crawling/docs/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**")
                .permitAll();

        // 나머지는 인증 필요
        auth.anyRequest().authenticated();
    }

    /** CORS 설정 */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (!corsProperties.getAllowedOrigins().isEmpty()) {
            configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        }
        if (!corsProperties.getAllowedMethods().isEmpty()) {
            configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        }
        if (!corsProperties.getAllowedHeaders().isEmpty()) {
            configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        }
        if (!corsProperties.getExposedHeaders().isEmpty()) {
            configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        }
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
