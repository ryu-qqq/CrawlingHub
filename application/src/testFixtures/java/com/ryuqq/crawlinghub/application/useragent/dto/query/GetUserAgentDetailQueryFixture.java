package com.ryuqq.crawlinghub.application.useragent.dto.query;

/**
 * GetUserAgentDetailQuery Test Fixture
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class GetUserAgentDetailQueryFixture {

    private static final Long DEFAULT_USER_AGENT_ID = 1L;

    /**
     * 기본 GetUserAgentDetailQuery 생성
     *
     * @return GetUserAgentDetailQuery
     */
    public static GetUserAgentDetailQuery create() {
        return new GetUserAgentDetailQuery(DEFAULT_USER_AGENT_ID);
    }

    /**
     * 특정 UserAgent ID로 GetUserAgentDetailQuery 생성
     *
     * @param userAgentId UserAgent ID
     * @return GetUserAgentDetailQuery
     */
    public static GetUserAgentDetailQuery createWithUserAgentId(Long userAgentId) {
        return new GetUserAgentDetailQuery(userAgentId);
    }
}

