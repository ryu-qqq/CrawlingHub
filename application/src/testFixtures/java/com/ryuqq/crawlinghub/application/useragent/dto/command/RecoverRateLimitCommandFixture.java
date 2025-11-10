package com.ryuqq.crawlinghub.application.useragent.dto.command;

/**
 * RecoverRateLimitCommand Test Fixture
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class RecoverRateLimitCommandFixture {

    private static final Long DEFAULT_USER_AGENT_ID = 1L;

    /**
     * 기본 RecoverRateLimitCommand 생성
     *
     * @return RecoverRateLimitCommand
     */
    public static RecoverRateLimitCommand create() {
        return new RecoverRateLimitCommand(DEFAULT_USER_AGENT_ID);
    }

    /**
     * 특정 UserAgent ID로 RecoverRateLimitCommand 생성
     *
     * @param userAgentId UserAgent ID
     * @return RecoverRateLimitCommand
     */
    public static RecoverRateLimitCommand createWithUserAgentId(Long userAgentId) {
        return new RecoverRateLimitCommand(userAgentId);
    }
}



