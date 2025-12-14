package com.ryuqq.crawlinghub.adapter.in.rest.auth.component;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 리소스 접근 권한 검사기
 *
 * <p>@PreAuthorize 어노테이션에서 SpEL 함수로 사용합니다.
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * // Controller 메서드에서 사용
 * @PreAuthorize("@access.hasPermission('seller:create')")
 * public void createSeller() { ... }
 *
 * @PreAuthorize("@access.hasAnyPermission('scheduler:read', 'scheduler:create')")
 * public void getScheduler() { ... }
 *
 * @PreAuthorize("@access.superAdmin()")
 * public void adminOnlyAction() { ... }
 * }</pre>
 *
 * <p>권한 체계:
 *
 * <ul>
 *   <li>SUPER_ADMIN: 모든 권한 bypass
 *   <li>권한 기반: {도메인}:{액션} 형식 (예: seller:create, scheduler:read)
 *   <li>역할 기반: ROLE_* 형식 (예: ROLE_ADMIN, ROLE_USER)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component("access")
public class ResourceAccessChecker {

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    /**
     * 현재 사용자가 SUPER_ADMIN인지 확인
     *
     * @return SUPER_ADMIN이면 true
     */
    public boolean superAdmin() {
        return SecurityContextHolder.hasRole(ROLE_SUPER_ADMIN);
    }

    /**
     * 특정 권한 보유 여부 확인
     *
     * <p>SUPER_ADMIN은 모든 권한을 가집니다.
     *
     * @param permission 확인할 권한 (예: seller:read, scheduler:create)
     * @return 권한이 있으면 true
     */
    public boolean hasPermission(String permission) {
        if (superAdmin()) {
            return true;
        }
        return SecurityContextHolder.hasPermission(permission);
    }

    /**
     * 여러 권한 중 하나라도 보유 여부 확인
     *
     * <p>SUPER_ADMIN은 모든 권한을 가집니다.
     *
     * @param permissions 확인할 권한들
     * @return 하나라도 있으면 true
     */
    public boolean hasAnyPermission(String... permissions) {
        if (superAdmin()) {
            return true;
        }
        return SecurityContextHolder.hasAnyPermission(permissions);
    }

    /**
     * 모든 권한 보유 여부 확인
     *
     * <p>SUPER_ADMIN은 모든 권한을 가집니다.
     *
     * @param permissions 확인할 권한들
     * @return 모두 있으면 true
     */
    public boolean hasAllPermissions(String... permissions) {
        if (superAdmin()) {
            return true;
        }
        return SecurityContextHolder.hasAllPermissions(permissions);
    }

    /**
     * 특정 역할 보유 여부 확인
     *
     * @param role 확인할 역할 (예: ROLE_ADMIN)
     * @return 역할이 있으면 true
     */
    public boolean hasRole(String role) {
        return SecurityContextHolder.hasRole(role);
    }

    /**
     * 여러 역할 중 하나라도 보유 여부 확인
     *
     * @param roles 확인할 역할들
     * @return 하나라도 있으면 true
     */
    public boolean hasAnyRole(String... roles) {
        return SecurityContextHolder.hasAnyRole(roles);
    }

    /**
     * 인증된 사용자인지 확인
     *
     * @return 인증되었으면 true
     */
    public boolean authenticated() {
        return SecurityContextHolder.isAuthenticated();
    }
}
