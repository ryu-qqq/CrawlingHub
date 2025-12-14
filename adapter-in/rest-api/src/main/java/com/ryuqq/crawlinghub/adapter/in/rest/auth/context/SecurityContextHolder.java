package com.ryuqq.crawlinghub.adapter.in.rest.auth.context;

/**
 * ThreadLocal 기반 SecurityContext 홀더
 *
 * <p>요청 스레드별로 SecurityContext를 관리합니다.
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * // 컨텍스트 설정 (Filter에서)
 * SecurityContextHolder.setContext(context);
 *
 * // 컨텍스트 조회 (Service/Controller에서)
 * SecurityContext context = SecurityContextHolder.getContext();
 * String userId = context.getUserId();
 *
 * // 컨텍스트 정리 (Filter finally에서)
 * SecurityContextHolder.clearContext();
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class SecurityContextHolder {

    private static final ThreadLocal<SecurityContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private SecurityContextHolder() {}

    /**
     * 현재 스레드의 SecurityContext 조회
     *
     * @return SecurityContext (없으면 anonymous)
     */
    public static SecurityContext getContext() {
        SecurityContext context = CONTEXT_HOLDER.get();
        return context != null ? context : SecurityContext.anonymous();
    }

    /**
     * 현재 스레드에 SecurityContext 설정
     *
     * @param context 설정할 SecurityContext
     */
    public static void setContext(SecurityContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 현재 스레드의 SecurityContext 제거
     *
     * <p>요청 처리 완료 후 반드시 호출해야 합니다. (메모리 누수 방지)
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     *
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        return getContext().isAuthenticated();
    }

    /**
     * 현재 사용자 ID 조회
     *
     * @return 사용자 ID (미인증 시 null)
     */
    public static String getCurrentUserId() {
        return getContext().getUserId();
    }

    /**
     * 현재 테넌트 ID 조회
     *
     * @return 테넌트 ID (미인증 시 null)
     */
    public static String getCurrentTenantId() {
        return getContext().getTenantId();
    }

    /**
     * 현재 조직 ID 조회
     *
     * @return 조직 ID (미인증 시 null)
     */
    public static String getCurrentOrganizationId() {
        return getContext().getOrganizationId();
    }

    /**
     * 특정 권한 보유 여부 확인
     *
     * @param permission 확인할 권한
     * @return 보유 여부
     */
    public static boolean hasPermission(String permission) {
        return getContext().hasPermission(permission);
    }

    /**
     * 여러 권한 중 하나라도 보유 여부 확인
     *
     * @param permissions 확인할 권한들
     * @return 하나라도 있으면 true
     */
    public static boolean hasAnyPermission(String... permissions) {
        SecurityContext context = getContext();
        for (String permission : permissions) {
            if (context.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 모든 권한 보유 여부 확인
     *
     * @param permissions 확인할 권한들
     * @return 모두 있으면 true
     */
    public static boolean hasAllPermissions(String... permissions) {
        SecurityContext context = getContext();
        for (String permission : permissions) {
            if (!context.hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 특정 역할 보유 여부 확인
     *
     * @param role 확인할 역할
     * @return 보유 여부
     */
    public static boolean hasRole(String role) {
        return getContext().hasRole(role);
    }

    /**
     * 여러 역할 중 하나라도 보유 여부 확인
     *
     * @param roles 확인할 역할들
     * @return 하나라도 있으면 true
     */
    public static boolean hasAnyRole(String... roles) {
        SecurityContext context = getContext();
        for (String role : roles) {
            if (context.hasRole(role)) {
                return true;
            }
        }
        return false;
    }
}
