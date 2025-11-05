package com.ryuqq.crawlinghub.application.useragent.dto.command;

/**
 * IssueTokenCommand Test Fixture
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class IssueTokenCommandFixture {

    private static final Long DEFAULT_USER_AGENT_ID = 1L;
    private static final String DEFAULT_TOKEN = "test-token-12345";

    /**
     * 기본 IssueTokenCommand 생성
     *
     * @return IssueTokenCommand
     */
    public static IssueTokenCommand create() {
        return new IssueTokenCommand(
            DEFAULT_USER_AGENT_ID,
            DEFAULT_TOKEN
        );
    }

    /**
     * 특정 UserAgent ID로 IssueTokenCommand 생성
     *
     * @param userAgentId UserAgent ID
     * @return IssueTokenCommand
     */
    public static IssueTokenCommand createWithUserAgentId(Long userAgentId) {
        return new IssueTokenCommand(
            userAgentId,
            DEFAULT_TOKEN
        );
    }

    /**
     * 특정 토큰으로 IssueTokenCommand 생성
     *
     * @param token 토큰
     * @return IssueTokenCommand
     */
    public static IssueTokenCommand createWithToken(String token) {
        return new IssueTokenCommand(
            DEFAULT_USER_AGENT_ID,
            token
        );
    }

    /**
     * 완전한 커스텀 IssueTokenCommand 생성
     *
     * @param userAgentId UserAgent ID
     * @param token 토큰
     * @return IssueTokenCommand
     */
    public static IssueTokenCommand createCustom(Long userAgentId, String token) {
        return new IssueTokenCommand(userAgentId, token);
    }
}

