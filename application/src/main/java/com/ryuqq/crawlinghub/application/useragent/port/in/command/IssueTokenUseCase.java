package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * 토큰 발급 UseCase (Lazy Token Issuance)
 *
 * <p>토큰이 없는 UserAgent에 토큰을 발급합니다.
 *
 * <p><strong>사용 시나리오</strong>:
 *
 * <ul>
 *   <li>UserAgent 등록 시 토큰을 발급하지 않은 경우
 *   <li>토큰이 필요한 시점에 요청으로 발급
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface IssueTokenUseCase {

    /**
     * UserAgent에 토큰 발급
     *
     * @param userAgentId 토큰을 발급할 UserAgent ID
     * @return 발급된 토큰의 암호화 값
     * @throws IllegalStateException 이미 토큰이 발급된 경우
     * @throws IllegalArgumentException UserAgent를 찾을 수 없는 경우
     */
    String issueToken(Long userAgentId);
}
