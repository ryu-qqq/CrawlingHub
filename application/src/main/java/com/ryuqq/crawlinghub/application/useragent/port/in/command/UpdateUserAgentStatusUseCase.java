package com.ryuqq.crawlinghub.application.useragent.port.in.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentStatusCommand;

/**
 * UserAgent 상태 일괄 변경 UseCase
 *
 * <p>관리자가 여러 UserAgent의 상태를 일괄 변경할 때 사용합니다.
 *
 * <p><strong>사용 시나리오</strong>:
 *
 * <ul>
 *   <li>관리자가 체크박스로 여러 UserAgent를 선택하여 상태 변경
 *   <li>문제가 있는 UserAgent들을 일괄 정지(SUSPENDED) 처리
 *   <li>점검 완료 후 일괄 활성화(AVAILABLE) 처리
 *   <li>보안 문제로 일괄 차단(BLOCKED) 처리
 * </ul>
 *
 * <p><strong>트랜잭션 규칙</strong>:
 *
 * <ul>
 *   <li>All or Nothing: 하나라도 실패하면 전체 롤백
 *   <li>Redis Cache와 DB 일관성 보장
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UpdateUserAgentStatusUseCase {

    /**
     * UserAgent 상태 일괄 변경
     *
     * @param command 상태 변경 Command (ID 목록 + 변경할 상태)
     * @return 변경된 UserAgent 수
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException 하나라도
     *     UserAgent를 찾을 수 없는 경우
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentStateException 동일한
     *     상태로 변경하려는 경우
     */
    int execute(UpdateUserAgentStatusCommand command);
}
