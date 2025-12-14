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
}
