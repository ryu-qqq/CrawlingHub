package com.ryuqq.crawlinghub.application.useragent.dto.command;

/**
 * DisableUserAgentCommand Test Fixture
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class DisableUserAgentCommandFixture {

    private static final Long DEFAULT_USER_AGENT_ID = 1L;

    /**
     * 기본 DisableUserAgentCommand 생성
     *
     * @return DisableUserAgentCommand
     */
    public static DisableUserAgentCommand create() {
        return new DisableUserAgentCommand(DEFAULT_USER_AGENT_ID);
    }

    /**
     * 특정 UserAgent ID로 DisableUserAgentCommand 생성
     *
     * @param userAgentId UserAgent ID
     * @return DisableUserAgentCommand
     */
    public static DisableUserAgentCommand createWithUserAgentId(Long userAgentId) {
        return new DisableUserAgentCommand(userAgentId);
    }
}



