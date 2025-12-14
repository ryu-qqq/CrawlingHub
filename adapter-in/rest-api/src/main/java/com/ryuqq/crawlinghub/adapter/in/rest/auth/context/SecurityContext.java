package com.ryuqq.crawlinghub.adapter.in.rest.auth.context;

import java.util.Set;

/**
 * 요청별 보안 컨텍스트
 *
 * <p>Gateway에서 전달한 인증 정보를 담는 컨텍스트 객체입니다.
 *
 * <p>포함 정보:
 *
 * <ul>
 *   <li>userId: 인증된 사용자 ID
 *   <li>tenantId: 테넌트 ID
 *   <li>organizationId: 조직 ID
 *   <li>roles: 사용자 역할 목록
 *   <li>permissions: 사용자 권한 목록
 *   <li>traceId: 분산 추적 ID
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class SecurityContext {

    private final String userId;
    private final String tenantId;
    private final String organizationId;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final String traceId;
    private final boolean authenticated;

    private SecurityContext(Builder builder) {
        this.userId = builder.userId;
        this.tenantId = builder.tenantId;
        this.organizationId = builder.organizationId;
        this.roles = builder.roles != null ? Set.copyOf(builder.roles) : Set.of();
        this.permissions = builder.permissions != null ? Set.copyOf(builder.permissions) : Set.of();
        this.traceId = builder.traceId;
        this.authenticated = builder.userId != null;
    }

    /**
     * Anonymous 컨텍스트 생성
     *
     * @return 미인증 컨텍스트
     */
    public static SecurityContext anonymous() {
        return new Builder().build();
    }

    /**
     * Builder 생성
     *
     * @return SecurityContext.Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public String getTraceId() {
        return traceId;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * 특정 역할 보유 여부 확인
     *
     * @param role 역할명
     * @return 보유 여부
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * 특정 권한 보유 여부 확인
     *
     * @param permission 권한명
     * @return 보유 여부
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /** SecurityContext Builder */
    public static final class Builder {
        private String userId;
        private String tenantId;
        private String organizationId;
        private Set<String> roles;
        private Set<String> permissions;
        private String traceId;

        private Builder() {}

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder organizationId(String organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder permissions(Set<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public SecurityContext build() {
            return new SecurityContext(this);
        }
    }
}
