package com.ryuqq.crawlinghub.adapter.in.rest.auth.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("rest-api")
@Tag("security")
@DisplayName("SecurityContext 단위 테스트")
class SecurityContextTest {

    @Nested
    @DisplayName("anonymous() 메서드는")
    class AnonymousMethod {

        @Test
        @DisplayName("미인증 SecurityContext를 생성한다")
        void shouldCreateAnonymousContext() {
            // When
            SecurityContext context = SecurityContext.anonymous();

            // Then
            assertThat(context.isAuthenticated()).isFalse();
            assertThat(context.getUserId()).isNull();
            assertThat(context.getTenantId()).isNull();
            assertThat(context.getOrganizationId()).isNull();
            assertThat(context.getRoles()).isEmpty();
            assertThat(context.getPermissions()).isEmpty();
            assertThat(context.getTraceId()).isNull();
        }
    }

    @Nested
    @DisplayName("Builder는")
    class BuilderTests {

        @Test
        @DisplayName("모든 필드를 설정할 수 있다")
        void shouldBuildWithAllFields() {
            // Given
            String userId = "user-123";
            String tenantId = "tenant-456";
            String organizationId = "org-789";
            Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_USER");
            Set<String> permissions = Set.of("crawling:read", "crawling:write");
            String traceId = "trace-abc";

            // When
            SecurityContext context =
                    SecurityContext.builder()
                            .userId(userId)
                            .tenantId(tenantId)
                            .organizationId(organizationId)
                            .roles(roles)
                            .permissions(permissions)
                            .traceId(traceId)
                            .build();

            // Then
            assertThat(context.getUserId()).isEqualTo(userId);
            assertThat(context.getTenantId()).isEqualTo(tenantId);
            assertThat(context.getOrganizationId()).isEqualTo(organizationId);
            assertThat(context.getRoles()).containsExactlyInAnyOrderElementsOf(roles);
            assertThat(context.getPermissions()).containsExactlyInAnyOrderElementsOf(permissions);
            assertThat(context.getTraceId()).isEqualTo(traceId);
            assertThat(context.isAuthenticated()).isTrue();
        }

        @Test
        @DisplayName("userId가 설정되면 authenticated가 true가 된다")
        void shouldBeAuthenticatedWhenUserIdIsSet() {
            // When
            SecurityContext context = SecurityContext.builder().userId("user-123").build();

            // Then
            assertThat(context.isAuthenticated()).isTrue();
        }

        @Test
        @DisplayName("userId가 null이면 authenticated가 false가 된다")
        void shouldNotBeAuthenticatedWhenUserIdIsNull() {
            // When
            SecurityContext context = SecurityContext.builder().build();

            // Then
            assertThat(context.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("roles가 null이면 빈 Set을 반환한다")
        void shouldReturnEmptySetWhenRolesIsNull() {
            // When
            SecurityContext context =
                    SecurityContext.builder().userId("user-123").roles(null).build();

            // Then
            assertThat(context.getRoles()).isEmpty();
        }

        @Test
        @DisplayName("permissions가 null이면 빈 Set을 반환한다")
        void shouldReturnEmptySetWhenPermissionsIsNull() {
            // When
            SecurityContext context =
                    SecurityContext.builder().userId("user-123").permissions(null).build();

            // Then
            assertThat(context.getPermissions()).isEmpty();
        }

        @Test
        @DisplayName("roles와 permissions는 불변 Set으로 복사된다")
        void shouldCopyRolesAndPermissionsAsImmutableSet() {
            // Given - mutable HashSet 사용 (Set.of()는 이미 불변이라 같은 인스턴스 반환 가능)
            Set<String> roles = new java.util.HashSet<>();
            roles.add("ROLE_ADMIN");
            Set<String> permissions = new java.util.HashSet<>();
            permissions.add("crawling:read");

            // When
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .roles(roles)
                            .permissions(permissions)
                            .build();

            // Then
            assertThat(context.getRoles()).isNotSameAs(roles);
            assertThat(context.getPermissions()).isNotSameAs(permissions);
            assertThat(context.getRoles()).containsExactly("ROLE_ADMIN");
            assertThat(context.getPermissions()).containsExactly("crawling:read");
        }
    }

    @Nested
    @DisplayName("hasRole() 메서드는")
    class HasRoleMethod {

        @Test
        @DisplayName("역할이 존재하면 true를 반환한다")
        void shouldReturnTrueWhenRoleExists() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                            .build();

            // When & Then
            assertThat(context.hasRole("ROLE_ADMIN")).isTrue();
            assertThat(context.hasRole("ROLE_USER")).isTrue();
        }

        @Test
        @DisplayName("역할이 존재하지 않으면 false를 반환한다")
        void shouldReturnFalseWhenRoleNotExists() {
            // Given
            SecurityContext context =
                    SecurityContext.builder().userId("user-123").roles(Set.of("ROLE_USER")).build();

            // When & Then
            assertThat(context.hasRole("ROLE_ADMIN")).isFalse();
        }

        @Test
        @DisplayName("역할이 비어있으면 false를 반환한다")
        void shouldReturnFalseWhenRolesEmpty() {
            // Given
            SecurityContext context = SecurityContext.builder().userId("user-123").build();

            // When & Then
            assertThat(context.hasRole("ROLE_ADMIN")).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPermission() 메서드는")
    class HasPermissionMethod {

        @Test
        @DisplayName("권한이 존재하면 true를 반환한다")
        void shouldReturnTrueWhenPermissionExists() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .permissions(Set.of("crawling:read", "crawling:write"))
                            .build();

            // When & Then
            assertThat(context.hasPermission("crawling:read")).isTrue();
            assertThat(context.hasPermission("crawling:write")).isTrue();
        }

        @Test
        @DisplayName("권한이 존재하지 않으면 false를 반환한다")
        void shouldReturnFalseWhenPermissionNotExists() {
            // Given
            SecurityContext context =
                    SecurityContext.builder()
                            .userId("user-123")
                            .permissions(Set.of("crawling:read"))
                            .build();

            // When & Then
            assertThat(context.hasPermission("crawling:delete")).isFalse();
        }

        @Test
        @DisplayName("권한이 비어있으면 false를 반환한다")
        void shouldReturnFalseWhenPermissionsEmpty() {
            // Given
            SecurityContext context = SecurityContext.builder().userId("user-123").build();

            // When & Then
            assertThat(context.hasPermission("crawling:read")).isFalse();
        }
    }
}
