package com.ryuqq.crawlinghub.application.common.tracing;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 분산 추적 컨텍스트
 *
 * <p>요청의 전체 흐름을 추적하기 위한 컨텍스트 정보를 담습니다. Gateway에서 전달받은 헤더를 기반으로 생성되며, 서버 간 통신 시 전파됩니다.
 *
 * <h3>추적 정보</h3>
 *
 * <ul>
 *   <li>traceId: 요청의 고유 식별자 (전체 흐름 추적)
 *   <li>userId: 요청을 수행한 사용자
 *   <li>tenantId: 사용자가 속한 테넌트
 *   <li>organizationId: 사용자가 속한 조직
 *   <li>roles: 사용자의 역할들
 *   <li>permissions: 사용자의 권한들
 *   <li>serviceName: 현재 서비스 이름
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class TracingContext {

    private final String traceId;
    private final String userId;
    private final String tenantId;
    private final String organizationId;
    private final String roles;
    private final String permissions;
    private final String serviceName;

    private TracingContext(Builder builder) {
        this.traceId = builder.traceId != null ? builder.traceId : generateTraceId();
        this.userId = builder.userId;
        this.tenantId = builder.tenantId;
        this.organizationId = builder.organizationId;
        this.roles = builder.roles;
        this.permissions = builder.permissions;
        this.serviceName = builder.serviceName;
    }

    public String getTraceId() {
        return traceId;
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

    public String getRoles() {
        return roles;
    }

    public String getPermissions() {
        return permissions;
    }

    public String getServiceName() {
        return serviceName;
    }

    /**
     * 인증된 사용자인지 확인
     *
     * @return userId가 있으면 true
     */
    public boolean isAuthenticated() {
        return userId != null && !userId.isBlank();
    }

    /**
     * 서버 간 통신용 헤더 맵 생성
     *
     * <p>외부 서비스 호출 시 전파할 헤더들을 반환합니다.
     *
     * @return 헤더 맵
     */
    public Map<String, String> toHeaders() {
        Map<String, String> headers = new HashMap<>();

        // 추적 ID는 항상 전파
        headers.put("X-Trace-Id", traceId);

        // 서비스 이름 전파
        if (serviceName != null) {
            headers.put("X-Service-Name", serviceName);
        }

        // 인증 정보 전파 (있는 경우에만)
        if (userId != null) {
            headers.put("X-User-Id", userId);
        }
        if (tenantId != null) {
            headers.put("X-Tenant-Id", tenantId);
        }
        if (organizationId != null) {
            headers.put("X-Organization-Id", organizationId);
        }
        if (roles != null) {
            headers.put("X-User-Roles", roles);
        }
        if (permissions != null) {
            headers.put("X-User-Permissions", permissions);
        }

        return headers;
    }

    /**
     * MDC에 설정할 값들 반환
     *
     * @return MDC 키-값 맵
     */
    public Map<String, String> toMdcMap() {
        Map<String, String> mdc = new HashMap<>();
        mdc.put("traceId", traceId);

        if (userId != null) {
            mdc.put("userId", userId);
        }
        if (tenantId != null) {
            mdc.put("tenantId", tenantId);
        }
        if (serviceName != null) {
            mdc.put("serviceName", serviceName);
        }

        return mdc;
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 빈 컨텍스트 생성 (시스템 작업용)
     *
     * @param serviceName 서비스 이름
     * @return 새로운 traceId가 생성된 컨텍스트
     */
    public static TracingContext system(String serviceName) {
        return builder().serviceName(serviceName).build();
    }

    public static final class Builder {
        private String traceId;
        private String userId;
        private String tenantId;
        private String organizationId;
        private String roles;
        private String permissions;
        private String serviceName;

        private Builder() {}

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

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

        public Builder roles(String roles) {
            this.roles = roles;
            return this;
        }

        public Builder permissions(String permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public TracingContext build() {
            return new TracingContext(this);
        }
    }
}
