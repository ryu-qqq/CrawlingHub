package com.ryuqq.crawlinghub.adapter.in.rest.auth.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.config.ServiceTokenProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * ServiceTokenAuthenticationFilter 단위 테스트
 *
 * <p>Service Token 기반 인증 필터의 인증 처리 로직을 검증합니다.
 *
 * <ul>
 *   <li>disabled 모드: anonymous 사용자에게 ROLE_SERVICE 부여
 *   <li>enabled + 토큰 일치: serviceName에 ROLE_SERVICE 부여
 *   <li>enabled + 토큰 불일치: SecurityContext 비워둠
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("ServiceTokenAuthenticationFilter 단위 테스트")
class ServiceTokenAuthenticationFilterTest {

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockFilterChain;

    @BeforeEach
    void setUp() {
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("인증 비활성화 모드 (enabled=false)")
    class DisabledModeTest {

        @Test
        @DisplayName("enabled=false이면 anonymous에 ROLE_SERVICE를 부여한다")
        void shouldGrantRoleServiceToAnonymousWhenDisabled() throws Exception {
            // Given
            ServiceTokenProperties properties = new ServiceTokenProperties(false, "secret");
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(properties);

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo("anonymous");
            assertThat(authentication.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE"));
        }

        @Test
        @DisplayName("enabled=false이면 헤더를 읽지 않고 필터 체인을 통과시킨다")
        void shouldPassThroughFilterChainWithoutReadingHeadersWhenDisabled() throws Exception {
            // Given
            ServiceTokenProperties properties = new ServiceTokenProperties(false, "secret");
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(properties);

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            verify(mockFilterChain).doFilter(mockRequest, mockResponse);
        }
    }

    @Nested
    @DisplayName("인증 활성화 모드 (enabled=true)")
    class EnabledModeTest {

        @Test
        @DisplayName("올바른 토큰과 serviceName이 있으면 ROLE_SERVICE를 부여한다")
        void shouldGrantRoleServiceWhenValidToken() throws Exception {
            // Given
            String secret = "valid-secret";
            String serviceName = "my-service";
            ServiceTokenProperties properties = new ServiceTokenProperties(true, secret);
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(properties);

            given(mockRequest.getHeader("X-Service-Token")).willReturn(secret);
            given(mockRequest.getHeader("X-Service-Name")).willReturn(serviceName);

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo(serviceName);
            assertThat(authentication.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE"));
        }

        @Test
        @DisplayName("올바른 토큰이지만 serviceName이 null이면 unknown-service를 주체로 사용한다")
        void shouldUseUnknownServiceWhenServiceNameIsNull() throws Exception {
            // Given
            String secret = "valid-secret";
            ServiceTokenProperties properties = new ServiceTokenProperties(true, secret);
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(properties);

            given(mockRequest.getHeader("X-Service-Token")).willReturn(secret);
            given(mockRequest.getHeader("X-Service-Name")).willReturn(null);

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo("unknown-service");
        }

        @Test
        @DisplayName("잘못된 토큰이면 SecurityContext가 비어있다")
        void shouldNotSetAuthenticationWhenInvalidToken() throws Exception {
            // Given
            ServiceTokenProperties properties = new ServiceTokenProperties(true, "correct-secret");
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(properties);

            given(mockRequest.getHeader("X-Service-Token")).willReturn("wrong-secret");
            given(mockRequest.getHeader("X-Service-Name")).willReturn("my-service");

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNull();
        }

        @Test
        @DisplayName("토큰이 null이면 SecurityContext가 비어있다")
        void shouldNotSetAuthenticationWhenTokenIsNull() throws Exception {
            // Given
            ServiceTokenProperties properties = new ServiceTokenProperties(true, "valid-secret");
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(properties);

            given(mockRequest.getHeader("X-Service-Token")).willReturn(null);
            given(mockRequest.getHeader("X-Service-Name")).willReturn("my-service");

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNull();
        }

        @Test
        @DisplayName("토큰 불일치 시에도 필터 체인은 계속 진행된다")
        void shouldContinueFilterChainEvenWhenTokenMismatch() throws Exception {
            // Given
            ServiceTokenProperties properties = new ServiceTokenProperties(true, "correct-secret");
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(properties);

            given(mockRequest.getHeader("X-Service-Token")).willReturn("wrong-secret");
            given(mockRequest.getHeader("X-Service-Name")).willReturn("my-service");

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            verify(mockFilterChain).doFilter(mockRequest, mockResponse);
        }

        @Test
        @DisplayName("빈 secret 설정 시 빈 토큰으로 인증할 수 있다")
        void shouldAuthenticateWithEmptySecretWhenConfiguredEmpty() throws Exception {
            // Given
            ServiceTokenProperties properties = new ServiceTokenProperties(true, "");
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(properties);

            given(mockRequest.getHeader("X-Service-Token")).willReturn("");
            given(mockRequest.getHeader("X-Service-Name")).willReturn("test-service");

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo("test-service");
        }
    }

    @Nested
    @DisplayName("필터 체인 진행 검증")
    class FilterChainTest {

        @Test
        @DisplayName("인증 성공 시에도 필터 체인이 계속 진행된다")
        void shouldContinueFilterChainOnAuthSuccess() throws Exception {
            // Given
            String secret = "secret";
            ServiceTokenProperties properties = new ServiceTokenProperties(true, secret);
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(properties);

            given(mockRequest.getHeader("X-Service-Token")).willReturn(secret);
            given(mockRequest.getHeader("X-Service-Name")).willReturn("service");

            // When
            filter.doFilter(mockRequest, mockResponse, mockFilterChain);

            // Then
            verify(mockFilterChain).doFilter(mockRequest, mockResponse);
        }
    }
}
