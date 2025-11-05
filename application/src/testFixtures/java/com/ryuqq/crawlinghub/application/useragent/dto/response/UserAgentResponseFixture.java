package com.ryuqq.crawlinghub.application.useragent.dto.response;

import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;

import java.time.LocalDateTime;

/**
 * UserAgentResponse Test Fixture
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class UserAgentResponseFixture {

    private static final Long DEFAULT_USER_AGENT_ID = 1L;
    private static final String DEFAULT_USER_AGENT_STRING = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    private static final String DEFAULT_TOKEN = "test-token-12345";
    private static final TokenStatus DEFAULT_STATUS = TokenStatus.IDLE;
    private static final Integer DEFAULT_REMAINING_REQUESTS = 80;

    /**
     * 기본 UserAgentResponse 생성
     *
     * @return UserAgentResponse
     */
    public static UserAgentResponse create() {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgentResponse(
            DEFAULT_USER_AGENT_ID,
            DEFAULT_USER_AGENT_STRING,
            DEFAULT_STATUS,
            DEFAULT_REMAINING_REQUESTS,
            now,
            null,
            now,
            now
        );
    }

    /**
     * 특정 상태로 UserAgentResponse 생성
     *
     * @param status TokenStatus
     * @return UserAgentResponse
     */
    public static UserAgentResponse createWithStatus(TokenStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgentResponse(
            DEFAULT_USER_AGENT_ID,
            DEFAULT_USER_AGENT_STRING,
            status,
            DEFAULT_REMAINING_REQUESTS,
            now,
            null,
            now,
            now
        );
    }

    /**
     * 완전한 커스텀 UserAgentResponse 생성
     *
     * @param userAgentId UserAgent ID
     * @param userAgentString User-Agent 문자열
     * @param tokenStatus 토큰 상태
     * @param remainingRequests 남은 요청 수
     * @return UserAgentResponse
     */
    public static UserAgentResponse createCustom(
        Long userAgentId,
        String userAgentString,
        TokenStatus tokenStatus,
        Integer remainingRequests
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgentResponse(
            userAgentId,
            userAgentString,
            tokenStatus,
            remainingRequests,
            now,
            null,
            now,
            now
        );
    }
}

