package com.ryuqq.crawlinghub.adapter.in.rest.auth.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.authhub.sdk.context.UserContext;
import com.ryuqq.authhub.sdk.context.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

@Tag("unit")
@Tag("rest-api")
@Tag("filter")
@DisplayName("GatewaySecurityBridgeFilter 단위 테스트")
class GatewaySecurityBridgeFilterTest {

    private GatewaySecurityBridgeFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new GatewaySecurityBridgeFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        UserContextHolder.clearContext();
    }

    @Nested
    @DisplayName("인증된 사용자 요청 처리")
    class AuthenticatedUserRequest {

        @Test
        @DisplayName("인증된 사용자의 경우 SecurityContextHolder에 Authentication이 설정된다")
        void shouldSetAuthenticationWhenUserIsAuthenticated() throws Exception {
            // Given
            UserContext context =
                    UserContext.builder()
                            .userId("user-001")
                            .roles(Set.of("ROLE_ADMIN"))
                            .permissions(Set.of("SELLER_READ"))
                            .build();
            UserContextHolder.setContext(context);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
        }

        @Test
        @DisplayName("인증된 사용자의 principal은 userId이다")
        void shouldSetUserIdAsPrincipal() throws Exception {
            // Given
            UserContext context =
                    UserContext.builder()
                            .userId("user-001")
                            .roles(Set.of("ROLE_ADMIN"))
                            .permissions(Set.of("SELLER_READ"))
                            .build();
            UserContextHolder.setContext(context);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication.getPrincipal()).isEqualTo("user-001");
        }

        @Test
        @DisplayName("인증된 사용자의 authorities에 role과 permission이 모두 포함된다")
        void shouldContainBothRolesAndPermissionsAsAuthorities() throws Exception {
            // Given
            UserContext context =
                    UserContext.builder()
                            .userId("user-001")
                            .roles(Set.of("ROLE_ADMIN"))
                            .permissions(Set.of("SELLER_READ"))
                            .build();
            UserContextHolder.setContext(context);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            assertThat(authorities).hasSize(2);
            assertThat(authorities)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder("ROLE_ADMIN", "SELLER_READ");
        }

        @Test
        @DisplayName("Authentication의 details에 UserContext가 설정된다")
        void shouldSetUserContextAsDetails() throws Exception {
            // Given
            UserContext context =
                    UserContext.builder()
                            .userId("user-001")
                            .roles(Set.of("ROLE_ADMIN"))
                            .permissions(Set.of("SELLER_READ"))
                            .build();
            UserContextHolder.setContext(context);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isInstanceOf(PreAuthenticatedAuthenticationToken.class);
            assertThat(authentication.getDetails()).isSameAs(context);
        }

        @Test
        @DisplayName("인증된 사용자 요청에서도 filterChain.doFilter가 반드시 호출된다")
        void shouldAlwaysCallDoFilterWhenAuthenticated() throws Exception {
            // Given
            UserContext context =
                    UserContext.builder()
                            .userId("user-001")
                            .roles(Set.of("ROLE_ADMIN"))
                            .permissions(Set.of("SELLER_READ"))
                            .build();
            UserContextHolder.setContext(context);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("인증되지 않은 사용자 요청 처리")
    class AnonymousUserRequest {

        @Test
        @DisplayName("인증되지 않은 사용자의 경우 SecurityContextHolder에 Authentication이 설정되지 않는다")
        void shouldNotSetAuthenticationWhenUserIsNotAuthenticated() throws Exception {
            // Given: UserContextHolder에 아무것도 설정하지 않으면 ANONYMOUS 컨텍스트 반환 (userId = null)

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNull();
        }

        @Test
        @DisplayName("인증되지 않은 사용자 요청에서도 filterChain.doFilter는 반드시 호출된다")
        void shouldAlwaysCallDoFilterWhenNotAuthenticated() throws Exception {
            // Given: UserContextHolder에 아무것도 설정하지 않으면 ANONYMOUS 컨텍스트 반환

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("빈 역할과 권한을 가진 인증된 사용자 처리")
    class AuthenticatedUserWithEmptyAuthorities {

        @Test
        @DisplayName("역할과 권한이 비어 있어도 Authentication은 빈 authorities로 설정된다")
        void shouldSetAuthenticationWithEmptyAuthoritiesWhenRolesAndPermissionsAreEmpty()
                throws Exception {
            // Given
            UserContext context = UserContext.builder().userId("user-002").build();
            UserContextHolder.setContext(context);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("역할과 권한이 비어 있어도 filterChain.doFilter는 반드시 호출된다")
        void shouldAlwaysCallDoFilterWhenAuthoritiesAreEmpty() throws Exception {
            // Given
            UserContext context = UserContext.builder().userId("user-002").build();
            UserContextHolder.setContext(context);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("다중 역할과 권한을 가진 인증된 사용자 처리")
    class AuthenticatedUserWithMultipleAuthorities {

        @Test
        @DisplayName("다중 역할과 권한이 authorities에 모두 포함된다")
        void shouldContainAllRolesAndPermissionsAsAuthorities() throws Exception {
            // Given
            UserContext context =
                    UserContext.builder()
                            .userId("user-003")
                            .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                            .permissions(Set.of("SELLER_READ", "SELLER_WRITE", "TASK_MANAGE"))
                            .build();
            UserContextHolder.setContext(context);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            assertThat(authorities).hasSize(5);
            assertThat(authorities)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder(
                            "ROLE_ADMIN",
                            "ROLE_USER",
                            "SELLER_READ",
                            "SELLER_WRITE",
                            "TASK_MANAGE");
        }

        @Test
        @DisplayName("다중 역할과 권한 처리 후에도 filterChain.doFilter는 반드시 호출된다")
        void shouldAlwaysCallDoFilterWithMultipleAuthorities() throws Exception {
            // Given
            UserContext context =
                    UserContext.builder()
                            .userId("user-003")
                            .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                            .permissions(Set.of("SELLER_READ", "SELLER_WRITE", "TASK_MANAGE"))
                            .build();
            UserContextHolder.setContext(context);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }
}
