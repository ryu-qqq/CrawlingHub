package com.ryuqq.crawlinghub.adapter.in.rest.auth.component;

import com.ryuqq.authhub.sdk.access.BaseAccessChecker;
import org.springframework.stereotype.Component;

/**
 * 리소스 접근 권한 검사기
 *
 * <p>AuthHub SDK의 BaseAccessChecker를 상속하여 @PreAuthorize 어노테이션에서 SpEL 함수로 사용합니다.
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
 * @see BaseAccessChecker
 */
@Component("access")
public class ResourceAccessChecker extends BaseAccessChecker {

    // SDK의 BaseAccessChecker가 superAdmin(), hasPermission(), hasRole(),
    // hasAnyPermission(), hasAllPermissions(), hasAnyRole(), authenticated(),
    // sameTenant(), sameOrganization(), myself(), myselfOr(), serviceAccount() 등 제공
    // 도메인별 확장 메서드가 필요하면 여기에 추가
}
