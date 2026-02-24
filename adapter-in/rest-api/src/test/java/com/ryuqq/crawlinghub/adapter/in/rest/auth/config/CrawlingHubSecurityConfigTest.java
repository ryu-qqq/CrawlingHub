package com.ryuqq.crawlinghub.adapter.in.rest.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.auth.filter.ServiceTokenAuthenticationFilter;
import jakarta.servlet.FilterChain;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * CrawlingHubSecurityConfig 단위 테스트
 *
 * <p>Security 설정의 writeErrorResponse 메서드와 ServiceTokenProperties 연동을 검증합니다. URL 패턴별 인증 요구사항은
 * ServiceTokenAuthenticationFilter 테스트에서 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("CrawlingHubSecurityConfig 단위 테스트")
class CrawlingHubSecurityConfigTest {

    private ObjectMapper objectMapper;
    private ServiceTokenProperties serviceTokenProperties;
    private CrawlingHubSecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        serviceTokenProperties = new ServiceTokenProperties(true, "test-secret");
        securityConfig = new CrawlingHubSecurityConfig(serviceTokenProperties, objectMapper);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("생성자 주입 검증")
    class ConstructorInjectionTest {

        @Test
        @DisplayName("serviceTokenProperties와 objectMapper를 주입받아 생성된다")
        void shouldCreateWithServiceTokenPropertiesAndObjectMapper() {
            // When
            CrawlingHubSecurityConfig config =
                    new CrawlingHubSecurityConfig(serviceTokenProperties, objectMapper);

            // Then
            assertThat(config).isNotNull();
        }
    }

    @Nested
    @DisplayName("ServiceTokenAuthenticationFilter 연동 검증")
    class FilterIntegrationTest {

        @Test
        @DisplayName("enabled=false이면 필터가 anonymous 인증을 설정한다")
        void shouldSetAnonymousAuthenticationWhenDisabled() throws Exception {
            // Given
            ServiceTokenProperties disabledProperties = new ServiceTokenProperties(false, "");
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(disabledProperties);

            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            // When
            filter.doFilter(request, response, chain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo("anonymous");
            assertThat(authentication.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE"));
        }

        @Test
        @DisplayName("enabled=true, 올바른 토큰이면 서비스 인증이 설정된다")
        void shouldSetServiceAuthenticationWithValidToken() throws Exception {
            // Given
            ServiceTokenProperties enabledProperties =
                    new ServiceTokenProperties(true, "my-secret");
            ServiceTokenAuthenticationFilter filter =
                    new ServiceTokenAuthenticationFilter(enabledProperties);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Service-Token", "my-secret");
            request.addHeader("X-Service-Name", "test-service");

            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            // When
            filter.doFilter(request, response, chain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo("test-service");
        }
    }

    @Nested
    @DisplayName("SecurityConfig 설정 검증")
    class SecurityConfigSetupTest {

        @Test
        @DisplayName("ServiceTokenProperties가 enabled=true, secret이 있는 상태로 올바르게 구성된다")
        void shouldConfigureWithEnabledAndSecret() {
            // Given
            ServiceTokenProperties props = new ServiceTokenProperties(true, "secure-secret");

            // When
            CrawlingHubSecurityConfig config = new CrawlingHubSecurityConfig(props, objectMapper);

            // Then
            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("ServiceTokenProperties가 enabled=false인 경우에도 올바르게 구성된다")
        void shouldConfigureWithDisabledServiceToken() {
            // Given
            ServiceTokenProperties disabledProps = new ServiceTokenProperties(false, null);

            // When
            CrawlingHubSecurityConfig config =
                    new CrawlingHubSecurityConfig(disabledProps, objectMapper);

            // Then
            assertThat(config).isNotNull();
        }
    }

    @Nested
    @DisplayName("writeErrorResponse() private 메서드 검증")
    class WriteErrorResponseTest {

        @Test
        @DisplayName("401 UNAUTHORIZED 에러 응답을 올바르게 작성한다")
        void shouldWrite401UnauthorizedResponse() throws Exception {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // writeErrorResponse 메서드를 리플렉션으로 호출
            Method writeErrorResponse =
                    CrawlingHubSecurityConfig.class.getDeclaredMethod(
                            "writeErrorResponse",
                            jakarta.servlet.http.HttpServletRequest.class,
                            jakarta.servlet.http.HttpServletResponse.class,
                            HttpStatus.class,
                            String.class,
                            String.class);
            writeErrorResponse.setAccessible(true);

            // When
            writeErrorResponse.invoke(
                    securityConfig,
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "인증이 필요합니다.");

            // Then
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentType()).contains("application/problem+json");
            assertThat(response.getHeader("x-error-code")).isEqualTo("UNAUTHORIZED");

            String body = response.getContentAsString();
            assertThat(body).contains("\"status\":401");
            assertThat(body).contains("UNAUTHORIZED");
            assertThat(body).contains("인증이 필요합니다.");
        }

        @Test
        @DisplayName("403 FORBIDDEN 에러 응답을 올바르게 작성한다")
        void shouldWrite403ForbiddenResponse() throws Exception {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            Method writeErrorResponse =
                    CrawlingHubSecurityConfig.class.getDeclaredMethod(
                            "writeErrorResponse",
                            jakarta.servlet.http.HttpServletRequest.class,
                            jakarta.servlet.http.HttpServletResponse.class,
                            HttpStatus.class,
                            String.class,
                            String.class);
            writeErrorResponse.setAccessible(true);

            // When
            writeErrorResponse.invoke(
                    securityConfig,
                    request,
                    response,
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "접근 권한이 없습니다.");

            // Then
            assertThat(response.getStatus()).isEqualTo(403);
            assertThat(response.getContentType()).contains("application/problem+json");
            assertThat(response.getHeader("x-error-code")).isEqualTo("FORBIDDEN");

            String body = response.getContentAsString();
            assertThat(body).contains("\"status\":403");
            assertThat(body).contains("FORBIDDEN");
            assertThat(body).contains("접근 권한이 없습니다.");
        }

        @Test
        @DisplayName("쿼리스트링이 있는 요청 URI는 instance에 포함된다")
        void shouldIncludeQueryStringInInstance() throws Exception {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/test");
            request.setQueryString("page=0&size=10");
            MockHttpServletResponse response = new MockHttpServletResponse();

            Method writeErrorResponse =
                    CrawlingHubSecurityConfig.class.getDeclaredMethod(
                            "writeErrorResponse",
                            jakarta.servlet.http.HttpServletRequest.class,
                            jakarta.servlet.http.HttpServletResponse.class,
                            HttpStatus.class,
                            String.class,
                            String.class);
            writeErrorResponse.setAccessible(true);

            // When
            writeErrorResponse.invoke(
                    securityConfig,
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "인증이 필요합니다.");

            // Then
            String body = response.getContentAsString();
            assertThat(body).contains("/api/test?page=0&size=10");
        }

        @Test
        @DisplayName("쿼리스트링이 공백이면 instance에 포함되지 않는다")
        void shouldNotIncludeBlankQueryStringInInstance() throws Exception {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/test");
            request.setQueryString("   ");
            MockHttpServletResponse response = new MockHttpServletResponse();

            Method writeErrorResponse =
                    CrawlingHubSecurityConfig.class.getDeclaredMethod(
                            "writeErrorResponse",
                            jakarta.servlet.http.HttpServletRequest.class,
                            jakarta.servlet.http.HttpServletResponse.class,
                            HttpStatus.class,
                            String.class,
                            String.class);
            writeErrorResponse.setAccessible(true);

            // When
            writeErrorResponse.invoke(
                    securityConfig,
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "인증이 필요합니다.");

            // Then
            String body = response.getContentAsString();
            // 공백 쿼리스트링은 isBlank() 조건에 걸려 URI에 추가되지 않음
            assertThat(body).doesNotContain("/api/test?");
            assertThat(body).contains("/api/test");
        }

        @Test
        @DisplayName("응답에 timestamp 필드가 포함된다")
        void shouldIncludeTimestampInResponse() throws Exception {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/test");
            MockHttpServletResponse response = new MockHttpServletResponse();

            Method writeErrorResponse =
                    CrawlingHubSecurityConfig.class.getDeclaredMethod(
                            "writeErrorResponse",
                            jakarta.servlet.http.HttpServletRequest.class,
                            jakarta.servlet.http.HttpServletResponse.class,
                            HttpStatus.class,
                            String.class,
                            String.class);
            writeErrorResponse.setAccessible(true);

            // When
            writeErrorResponse.invoke(
                    securityConfig,
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "인증이 필요합니다.");

            // Then
            String body = response.getContentAsString();
            assertThat(body).contains("timestamp");
            assertThat(body).contains("about:blank");
            assertThat(body).contains("type");
            assertThat(body).contains("title");
            assertThat(body).contains("detail");
        }
    }
}
