package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * 개별 UserAgent 정지 UseCase
 *
 * <p>특정 UserAgent를 수동으로 SUSPENDED 상태로 전환합니다.
 *
 * <p><strong>사용 시나리오</strong>:
 *
 * <ul>
 *   <li>관리자가 특정 UserAgent를 일시적으로 비활성화할 때
 *   <li>의심스러운 행동이 감지된 UserAgent를 격리할 때
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SuspendUserAgentUseCase {

    /**
     * 특정 UserAgent 정지
     *
     * @param userAgentId UserAgent ID
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException
     *     UserAgent가 없는 경우
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentStateException 이미
     *     SUSPENDED/BLOCKED 상태인 경우
     */
    void execute(long userAgentId);
}
