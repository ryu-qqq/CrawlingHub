package com.ryuqq.crawlinghub.adapter.in.rest.auth.paths;

import java.util.List;

/**
 * 보안 경로 분류 상수 정의
 *
 * <p>접근 권한별 경로를 정의합니다. SecurityConfig에서 참조하여 권한 설정에 사용됩니다.
 *
 * <p>경로 분류:
 *
 * <ul>
 *   <li>PUBLIC: 인증 불필요 (헬스체크)
 *   <li>DOCS: 인증된 사용자면 접근 가능 (API 문서)
 *   <li>AUTHENTICATED: 인증된 사용자 + @PreAuthorize 권한 검사 (관리 API)
 * </ul>
 *
 * <p>권한 처리:
 *
 * <ul>
 *   <li>URL 기반 역할 검사 제거 → @PreAuthorize 어노테이션으로 대체
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class SecurityPaths {

    private SecurityPaths() {}

    /**
     * 인증 불필요 경로 (Public)
     *
     * <p>JWT 검증 없이 접근 가능한 경로입니다.
     */
    public static final class Public {

        /** Public 경로 목록 */
        public static final List<String> PATTERNS =
                List.of(
                        // 헬스체크
                        ApiPaths.Actuator.BASE + "/**", ApiPaths.Health.CHECK);

        private Public() {}
    }

    /**
     * API 문서 경로 (Swagger, REST Docs)
     *
     * <p>인증된 사용자면 접근 가능한 API 문서 경로입니다.
     */
    public static final class Docs {

        /** API 문서 경로 목록 */
        public static final List<String> PATTERNS =
                List.of(
                        ApiPaths.OpenApi.SWAGGER_REDIRECT,
                        ApiPaths.OpenApi.SWAGGER_UI,
                        ApiPaths.OpenApi.SWAGGER_UI_HTML,
                        ApiPaths.OpenApi.DOCS,
                        ApiPaths.Docs.BASE,
                        ApiPaths.Docs.ALL);

        private Docs() {}
    }

    /**
     * 헤더 상수
     *
     * <p>Gateway에서 전달하는 인증 정보 헤더입니다.
     */
    public static final class Headers {

        /** 사용자 ID 헤더 - Gateway에서 JWT userId 클레임 추출 */
        public static final String USER_ID = "X-User-Id";

        /** 테넌트 ID 헤더 - Gateway에서 JWT tenantId 클레임 추출 */
        public static final String TENANT_ID = "X-Tenant-Id";

        /** 조직 ID 헤더 - Gateway에서 JWT organizationId 클레임 추출 */
        public static final String ORGANIZATION_ID = "X-Organization-Id";

        /** 역할 헤더 - 콤마 구분 형식 (예: SUPER_ADMIN,ADMIN) */
        public static final String ROLES = "X-User-Roles";

        /** 권한 헤더 - 콤마 구분 형식 (예: crawling:read,crawling:write) */
        public static final String PERMISSIONS = "X-Permissions";

        /** 추적 ID 헤더 - 분산 추적용 */
        public static final String TRACE_ID = "X-Trace-Id";

        /** 서비스 토큰 헤더 - Internal API 인증용 */
        public static final String SERVICE_TOKEN = "X-Service-Token";

        private Headers() {}
    }
}
