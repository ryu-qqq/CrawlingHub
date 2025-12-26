package com.ryuqq.crawlinghub.integration.helper;

import org.springframework.http.HttpHeaders;

/**
 * 통합 테스트용 인증 헬퍼
 *
 * <p>Gateway 인증 헤더를 생성하여 테스트에서 인증된 요청을 시뮬레이션합니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * HttpHeaders headers = AuthTestHelper.withPermissions("task:read", "task:update");
 * restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), responseType);
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class AuthTestHelper {

    /** Gateway 인증 헤더 상수 */
    public static final String HEADER_USER_ID = "X-User-Id";

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_ORGANIZATION_ID = "X-Organization-Id";
    public static final String HEADER_ROLES = "X-User-Roles";
    public static final String HEADER_PERMISSIONS = "X-Permissions";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /** 기본 테스트 사용자 정보 */
    public static final String DEFAULT_USER_ID = "test-user-1";

    public static final String DEFAULT_TENANT_ID = "test-tenant-1";
    public static final String DEFAULT_ORGANIZATION_ID = "test-org-1";

    private AuthTestHelper() {}

    /**
     * 기본 인증 헤더 생성 (인증만, 권한 없음)
     *
     * @return 기본 인증 헤더
     */
    public static HttpHeaders authenticated() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER_ID, DEFAULT_USER_ID);
        headers.set(HEADER_TENANT_ID, DEFAULT_TENANT_ID);
        headers.set(HEADER_ORGANIZATION_ID, DEFAULT_ORGANIZATION_ID);
        headers.set(HEADER_TRACE_ID, "test-trace-" + System.currentTimeMillis());
        return headers;
    }

    /**
     * 특정 권한을 가진 인증 헤더 생성
     *
     * @param permissions 부여할 권한들 (예: "task:read", "task:update")
     * @return 권한이 포함된 인증 헤더
     */
    public static HttpHeaders withPermissions(String... permissions) {
        HttpHeaders headers = authenticated();
        headers.set(HEADER_PERMISSIONS, String.join(",", permissions));
        return headers;
    }

    /**
     * 특정 역할을 가진 인증 헤더 생성
     *
     * @param roles 부여할 역할들 (예: "ADMIN", "USER")
     * @return 역할이 포함된 인증 헤더
     */
    public static HttpHeaders withRoles(String... roles) {
        HttpHeaders headers = authenticated();
        headers.set(HEADER_ROLES, String.join(",", roles));
        return headers;
    }

    /**
     * SUPER_ADMIN 역할 헤더 생성
     *
     * <p>SUPER_ADMIN은 모든 권한을 bypass합니다.
     *
     * @return SUPER_ADMIN 역할 헤더
     */
    public static HttpHeaders superAdmin() {
        HttpHeaders headers = authenticated();
        headers.set(HEADER_ROLES, "SUPER_ADMIN");
        return headers;
    }

    /**
     * 권한과 역할을 모두 가진 인증 헤더 생성
     *
     * @param roles 부여할 역할들
     * @param permissions 부여할 권한들
     * @return 역할과 권한이 포함된 인증 헤더
     */
    public static HttpHeaders withRolesAndPermissions(String[] roles, String[] permissions) {
        HttpHeaders headers = authenticated();
        if (roles != null && roles.length > 0) {
            headers.set(HEADER_ROLES, String.join(",", roles));
        }
        if (permissions != null && permissions.length > 0) {
            headers.set(HEADER_PERMISSIONS, String.join(",", permissions));
        }
        return headers;
    }

    /**
     * 커스텀 사용자 ID로 인증 헤더 생성
     *
     * @param userId 사용자 ID
     * @param permissions 부여할 권한들
     * @return 커스텀 사용자의 인증 헤더
     */
    public static HttpHeaders withUserAndPermissions(String userId, String... permissions) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER_ID, userId);
        headers.set(HEADER_TENANT_ID, DEFAULT_TENANT_ID);
        headers.set(HEADER_ORGANIZATION_ID, DEFAULT_ORGANIZATION_ID);
        headers.set(HEADER_TRACE_ID, "test-trace-" + System.currentTimeMillis());
        if (permissions != null && permissions.length > 0) {
            headers.set(HEADER_PERMISSIONS, String.join(",", permissions));
        }
        return headers;
    }

    /**
     * 인증되지 않은 요청을 위한 빈 헤더
     *
     * @return 빈 헤더 (인증 없음)
     */
    public static HttpHeaders unauthenticated() {
        return new HttpHeaders();
    }
}
