package com.ryuqq.crawlinghub.adapter.in.rest.auth.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.context.SecurityContext;
import com.ryuqq.crawlinghub.adapter.in.rest.auth.context.SecurityContextHolder;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("rest-api")
@Tag("security")
@DisplayName("ResourceAccessChecker 단위 테스트")
class ResourceAccessCheckerTest {

    private ResourceAccessChecker resourceAccessChecker;

    @BeforeEach
    void setUp() {
        resourceAccessChecker = new ResourceAccessChecker();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("superAdmin() 메서드는")
    class SuperAdminMethod {

        @Test
        @DisplayName("ROLE_SUPER_ADMIN 역할이 있으면 true를 반환한다")
        void shouldReturnTrueWhenHasSuperAdminRole() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("admin-user")
                            .roles(Set.of("ROLE_SUPER_ADMIN"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.superAdmin()).isTrue();
        }

        @Test
        @DisplayName("ROLE_SUPER_ADMIN 역할이 없으면 false를 반환한다")
        void shouldReturnFalseWhenNoSuperAdminRole() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("regular-user")
                            .roles(Set.of("ROLE_USER"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.superAdmin()).isFalse();
        }

        @Test
        @DisplayName("미인증 상태면 false를 반환한다")
        void shouldReturnFalseWhenNotAuthenticated() {
            // When & Then
            assertThat(resourceAccessChecker.superAdmin()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPermission() 메서드는")
    class HasPermissionMethod {

        @Test
        @DisplayName("SUPER_ADMIN이면 모든 권한에 대해 true를 반환한다")
        void shouldReturnTrueForAnyPermissionWhenSuperAdmin() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("admin-user")
                            .roles(Set.of("ROLE_SUPER_ADMIN"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasPermission("seller:create")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("scheduler:delete")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("any:permission")).isTrue();
        }

        @Test
        @DisplayName("해당 권한이 있으면 true를 반환한다")
        void shouldReturnTrueWhenHasPermission() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .permissions(Set.of("seller:create", "seller:read"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasPermission("seller:create")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("seller:read")).isTrue();
        }

        @Test
        @DisplayName("해당 권한이 없으면 false를 반환한다")
        void shouldReturnFalseWhenNoPermission() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .permissions(Set.of("seller:read"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasPermission("seller:create")).isFalse();
            assertThat(resourceAccessChecker.hasPermission("scheduler:read")).isFalse();
        }

        @Test
        @DisplayName("미인증 상태면 false를 반환한다")
        void shouldReturnFalseWhenNotAuthenticated() {
            // When & Then
            assertThat(resourceAccessChecker.hasPermission("seller:create")).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAnyPermission() 메서드는")
    class HasAnyPermissionMethod {

        @Test
        @DisplayName("SUPER_ADMIN이면 true를 반환한다")
        void shouldReturnTrueWhenSuperAdmin() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("admin-user")
                            .roles(Set.of("ROLE_SUPER_ADMIN"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasAnyPermission("seller:create", "scheduler:read"))
                    .isTrue();
        }

        @Test
        @DisplayName("하나 이상의 권한이 있으면 true를 반환한다")
        void shouldReturnTrueWhenHasAtLeastOnePermission() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .permissions(Set.of("seller:create"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasAnyPermission("seller:create", "scheduler:read"))
                    .isTrue();
        }

        @Test
        @DisplayName("모든 권한이 없으면 false를 반환한다")
        void shouldReturnFalseWhenNoPermissions() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .permissions(Set.of("task:read"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasAnyPermission("seller:create", "scheduler:read"))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("hasAllPermissions() 메서드는")
    class HasAllPermissionsMethod {

        @Test
        @DisplayName("SUPER_ADMIN이면 true를 반환한다")
        void shouldReturnTrueWhenSuperAdmin() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("admin-user")
                            .roles(Set.of("ROLE_SUPER_ADMIN"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasAllPermissions("seller:create", "scheduler:read"))
                    .isTrue();
        }

        @Test
        @DisplayName("모든 권한이 있으면 true를 반환한다")
        void shouldReturnTrueWhenHasAllPermissions() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .permissions(Set.of("seller:create", "scheduler:read", "task:read"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasAllPermissions("seller:create", "scheduler:read"))
                    .isTrue();
        }

        @Test
        @DisplayName("일부 권한만 있으면 false를 반환한다")
        void shouldReturnFalseWhenMissingSomePermissions() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .permissions(Set.of("seller:create"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasAllPermissions("seller:create", "scheduler:read"))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("hasRole() 메서드는")
    class HasRoleMethod {

        @Test
        @DisplayName("해당 역할이 있으면 true를 반환한다")
        void shouldReturnTrueWhenHasRole() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasRole("ROLE_ADMIN")).isTrue();
            assertThat(resourceAccessChecker.hasRole("ROLE_USER")).isTrue();
        }

        @Test
        @DisplayName("해당 역할이 없으면 false를 반환한다")
        void shouldReturnFalseWhenNoRole() {
            // Given
            SecurityContext context =
                    SecurityContext.builder().userId("user-123").roles(Set.of("ROLE_USER")).build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasRole("ROLE_ADMIN")).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAnyRole() 메서드는")
    class HasAnyRoleMethod {

        @Test
        @DisplayName("하나 이상의 역할이 있으면 true를 반환한다")
        void shouldReturnTrueWhenHasAtLeastOneRole() {
            // Given
            SecurityContext context =
                    SecurityContext.builder().userId("user-123").roles(Set.of("ROLE_USER")).build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasAnyRole("ROLE_ADMIN", "ROLE_USER")).isTrue();
        }

        @Test
        @DisplayName("모든 역할이 없으면 false를 반환한다")
        void shouldReturnFalseWhenNoRoles() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .roles(Set.of("ROLE_GUEST"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.hasAnyRole("ROLE_ADMIN", "ROLE_USER")).isFalse();
        }
    }

    @Nested
    @DisplayName("authenticated() 메서드는")
    class AuthenticatedMethod {

        @Test
        @DisplayName("인증된 상태면 true를 반환한다")
        void shouldReturnTrueWhenAuthenticated() {
            // Given
            SecurityContext context = SecurityContext.builder().userId("user-123").build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.authenticated()).isTrue();
        }

        @Test
        @DisplayName("미인증 상태면 false를 반환한다")
        void shouldReturnFalseWhenNotAuthenticated() {
            // When & Then
            assertThat(resourceAccessChecker.authenticated()).isFalse();
        }
    }

    @Nested
    @DisplayName("통합 시나리오")
    class IntegrationScenarios {

        @Test
        @DisplayName("일반 사용자가 seller 도메인 권한만 가진 경우")
        void sellerManagerScenario() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("seller-manager-001")
                            .roles(Set.of("ROLE_USER"))
                            .permissions(Set.of("seller:create", "seller:read", "seller:update"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.authenticated()).isTrue();
            assertThat(resourceAccessChecker.superAdmin()).isFalse();

            assertThat(resourceAccessChecker.hasPermission("seller:create")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("seller:read")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("seller:update")).isTrue();

            assertThat(resourceAccessChecker.hasPermission("scheduler:create")).isFalse();
            assertThat(resourceAccessChecker.hasPermission("task:read")).isFalse();
        }

        @Test
        @DisplayName("SUPER_ADMIN은 모든 권한에 접근 가능")
        void superAdminScenario() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("super-admin-001")
                            .roles(Set.of("ROLE_SUPER_ADMIN"))
                            .build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(resourceAccessChecker.authenticated()).isTrue();
            assertThat(resourceAccessChecker.superAdmin()).isTrue();

            assertThat(resourceAccessChecker.hasPermission("seller:create")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("scheduler:create")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("task:read")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("execution:read")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("useragent:manage")).isTrue();
            assertThat(resourceAccessChecker.hasPermission("any:permission")).isTrue();
        }
    }
}
