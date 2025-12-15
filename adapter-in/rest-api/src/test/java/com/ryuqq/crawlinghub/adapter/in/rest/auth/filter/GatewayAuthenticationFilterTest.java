package com.ryuqq.crawlinghub.adapter.in.rest.auth.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.context.SecurityContext;
import com.ryuqq.crawlinghub.adapter.in.rest.auth.context.SecurityContextHolder;
import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.SecurityPaths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

@Tag("unit")
@Tag("rest-api")
@Tag("security")
@ExtendWith(MockitoExtension.class)
@DisplayName("GatewayAuthenticationFilter 단위 테스트")
class GatewayAuthenticationFilterTest {

    private GatewayAuthenticationFilter filter;

    @Mock private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new GatewayAuthenticationFilter();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("doFilterInternal() 메서드는")
    class DoFilterInternalMethod {

        @Test
        @DisplayName("X-User-Id 헤더가 없으면 anonymous 컨텍스트를 설정한다")
        void shouldSetAnonymousContextWhenNoUserIdHeader() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<Boolean> authenticated = new AtomicReference<>();

            doAnswer(
                            inv -> {
                                authenticated.set(SecurityContextHolder.isAuthenticated());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(authenticated.get()).isFalse();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("X-User-Id 헤더가 있으면 인증된 컨텍스트를 설정한다")
        void shouldSetAuthenticatedContextWhenUserIdHeaderPresent()
                throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();

            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().isAuthenticated()).isTrue();
            assertThat(capturedContext.get().getUserId()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("모든 Gateway 헤더를 파싱하여 컨텍스트에 설정한다")
        void shouldParseAllGatewayHeaders() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(SecurityPaths.Headers.TENANT_ID, "tenant-456");
            request.addHeader(SecurityPaths.Headers.ORGANIZATION_ID, "org-789");
            request.addHeader(SecurityPaths.Headers.ROLES, "ADMIN,USER");
            request.addHeader(SecurityPaths.Headers.PERMISSIONS, "crawling:read,crawling:write");
            request.addHeader(SecurityPaths.Headers.TRACE_ID, "trace-abc");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();

            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            SecurityContext context = capturedContext.get();
            assertThat(context.getUserId()).isEqualTo("user-123");
            assertThat(context.getTenantId()).isEqualTo("tenant-456");
            assertThat(context.getOrganizationId()).isEqualTo("org-789");
            assertThat(context.getRoles()).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
            assertThat(context.getPermissions())
                    .containsExactlyInAnyOrder("crawling:read", "crawling:write");
            assertThat(context.getTraceId()).isEqualTo("trace-abc");
        }

        @Test
        @DisplayName("필터 처리 후 컨텍스트를 정리한다")
        void shouldClearContextAfterFilter() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            MockHttpServletResponse response = new MockHttpServletResponse();

            doAnswer(inv -> null)
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(SecurityContextHolder.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("예외 발생 시에도 컨텍스트를 정리한다")
        void shouldClearContextEvenOnException() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            MockHttpServletResponse response = new MockHttpServletResponse();

            doAnswer(
                            inv -> {
                                throw new RuntimeException("Test exception");
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            try {
                filter.doFilterInternal(request, response, filterChain);
            } catch (Exception ignored) {
                // Expected exception
            }

            // Then
            assertThat(SecurityContextHolder.isAuthenticated()).isFalse();
        }
    }

    @Nested
    @DisplayName("X-User-Roles 헤더 파싱")
    class RolesHeaderParsing {

        @Test
        @DisplayName("콤마로 구분된 역할을 파싱한다")
        void shouldParseCommaSeparatedRoles() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(SecurityPaths.Headers.ROLES, "ADMIN,USER");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getRoles())
                    .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        }

        @Test
        @DisplayName("ROLE_ 접두사가 없으면 자동으로 추가한다")
        void shouldAddRolePrefixAutomatically() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(SecurityPaths.Headers.ROLES, "SUPER_ADMIN,ADMIN");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getRoles())
                    .containsExactlyInAnyOrder("ROLE_SUPER_ADMIN", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("ROLE_ 접두사가 이미 있으면 중복 추가하지 않는다")
        void shouldNotDuplicateRolePrefix() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(SecurityPaths.Headers.ROLES, "ROLE_ADMIN,USER");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getRoles())
                    .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        }

        @Test
        @DisplayName("공백이 있는 역할 문자열을 트림하여 파싱한다")
        void shouldTrimRoles() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(SecurityPaths.Headers.ROLES, " ADMIN , USER ");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getRoles())
                    .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        }

        @Test
        @DisplayName("X-User-Roles 헤더가 없으면 빈 Set을 반환한다")
        void shouldReturnEmptySetWhenNoRolesHeader() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getRoles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("X-Permissions 헤더 파싱")
    class PermissionsHeaderParsing {

        @Test
        @DisplayName("콤마로 구분된 권한을 파싱한다")
        void shouldParseCommaSeparatedPermissions() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(
                    SecurityPaths.Headers.PERMISSIONS,
                    "crawling:read,crawling:write,crawling:delete");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getPermissions())
                    .containsExactlyInAnyOrder(
                            "crawling:read", "crawling:write", "crawling:delete");
        }

        @Test
        @DisplayName("공백이 있는 권한 문자열을 트림하여 파싱한다")
        void shouldTrimPermissions() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(
                    SecurityPaths.Headers.PERMISSIONS, " crawling:read , crawling:write ");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getPermissions())
                    .containsExactlyInAnyOrder("crawling:read", "crawling:write");
        }

        @Test
        @DisplayName("X-Permissions 헤더가 없으면 빈 Set을 반환한다")
        void shouldReturnEmptySetWhenNoPermissionsHeader() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getPermissions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Spring Security 동기화")
    class SpringSecuritySynchronization {

        @Test
        @DisplayName("인증 시 Spring Security Context에도 Authentication을 설정한다")
        void shouldSynchronizeWithSpringSecurityContext() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(SecurityPaths.Headers.ROLES, "ADMIN");
            request.addHeader(SecurityPaths.Headers.PERMISSIONS, "crawling:read");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<Authentication> capturedAuth = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedAuth.set(
                                        org.springframework.security.core.context
                                                .SecurityContextHolder.getContext()
                                                .getAuthentication());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication auth = capturedAuth.get();
            assertThat(auth).isNotNull();
            assertThat(auth.getPrincipal()).isEqualTo("user-123");
            assertThat(auth.getAuthorities())
                    .extracting("authority")
                    .containsExactlyInAnyOrder("ROLE_ADMIN", "crawling:read");
        }

        @Test
        @DisplayName("필터 후 Spring Security Context도 정리한다")
        void shouldClearSpringSecurityContextAfterFilter() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            MockHttpServletResponse response = new MockHttpServletResponse();

            doAnswer(inv -> null)
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(
                            org.springframework.security.core.context.SecurityContextHolder
                                    .getContext()
                                    .getAuthentication())
                    .isNull();
        }
    }

    @Nested
    @DisplayName("빈 헤더 처리")
    class EmptyHeaderHandling {

        @Test
        @DisplayName("빈 문자열 X-Tenant-Id는 null로 처리한다")
        void shouldTreatEmptyTenantIdAsNull() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(SecurityPaths.Headers.TENANT_ID, "");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getTenantId()).isNull();
        }

        @Test
        @DisplayName("공백만 있는 X-Organization-Id는 null로 처리한다")
        void shouldTreatWhitespaceOrganizationIdAsNull() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(SecurityPaths.Headers.USER_ID, "user-123");
            request.addHeader(SecurityPaths.Headers.ORGANIZATION_ID, "   ");
            MockHttpServletResponse response = new MockHttpServletResponse();

            AtomicReference<SecurityContext> capturedContext = new AtomicReference<>();
            doAnswer(
                            inv -> {
                                capturedContext.set(SecurityContextHolder.getContext());
                                return null;
                            })
                    .when(filterChain)
                    .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(capturedContext.get().getOrganizationId()).isNull();
        }
    }
}
