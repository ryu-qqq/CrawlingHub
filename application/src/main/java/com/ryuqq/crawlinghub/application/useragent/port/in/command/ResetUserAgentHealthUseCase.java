package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * UserAgent Health Score 리셋 UseCase
 *
 * <p>특정 UserAgent의 Health Score를 100으로 리셋합니다.
 *
 * <p><strong>사용 시나리오</strong>:
 *
 * <ul>
 *   <li>테스트 목적으로 UserAgent를 초기 상태로 되돌릴 때
 *   <li>일시적 네트워크 문제로 Health Score가 떨어진 경우 관리자가 수동 복구할 때
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ResetUserAgentHealthUseCase {

    /**
     * 특정 UserAgent의 Health Score를 100으로 리셋
     *
     * @param userAgentId UserAgent ID
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException
     *     UserAgent가 없는 경우
     */
    void execute(long userAgentId);
}
