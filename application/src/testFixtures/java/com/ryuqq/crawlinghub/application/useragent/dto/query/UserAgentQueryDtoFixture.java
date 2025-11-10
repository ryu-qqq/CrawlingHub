package com.ryuqq.crawlinghub.application.useragent.dto.query;

import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;

import java.time.LocalDateTime;

/**
 * UserAgentQueryDto Test Fixture
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class UserAgentQueryDtoFixture {

    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_USER_AGENT_STRING = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    private static final String DEFAULT_TOKEN = "test-token-12345";
    private static final TokenStatus DEFAULT_STATUS = TokenStatus.IDLE;
    private static final Integer DEFAULT_REMAINING_REQUESTS = 80;

    /**
     * 기본 UserAgentQueryDto 생성
     *
     * @return UserAgentQueryDto
     */
    public static UserAgentQueryDto create() {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgentQueryDto(
            DEFAULT_ID,
            DEFAULT_USER_AGENT_STRING,
            DEFAULT_TOKEN,
            DEFAULT_STATUS,
            DEFAULT_REMAINING_REQUESTS,
            now,
            null,
            now,
            now
        );
    }

    /**
     * 특정 상태로 UserAgentQueryDto 생성
     *
     * @param status TokenStatus
     * @return UserAgentQueryDto
     */
    public static UserAgentQueryDto createWithStatus(TokenStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgentQueryDto(
            DEFAULT_ID,
            DEFAULT_USER_AGENT_STRING,
            DEFAULT_TOKEN,
            status,
            DEFAULT_REMAINING_REQUESTS,
            now,
            null,
            now,
            now
        );
    }

    /**
     * 요청 가능한 UserAgentQueryDto 생성 (로테이션용)
     *
     * @param remainingRequests 남은 요청 수
     * @return UserAgentQueryDto
     */
    public static UserAgentQueryDto createAvailableForRotation(int remainingRequests) {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgentQueryDto(
            DEFAULT_ID,
            DEFAULT_USER_AGENT_STRING,
            DEFAULT_TOKEN,
            TokenStatus.IDLE,
            remainingRequests,
            now,
            null,
            now,
            now
        );
    }

    /**
     * 완전한 커스텀 UserAgentQueryDto 생성
     *
     * @param id UserAgent ID
     * @param userAgentString User-Agent 문자열
     * @param tokenStatus 토큰 상태
     * @param remainingRequests 남은 요청 수
     * @return UserAgentQueryDto
     */
    public static UserAgentQueryDto createCustom(
        Long id,
        String userAgentString,
        TokenStatus tokenStatus,
        Integer remainingRequests
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgentQueryDto(
            id,
            userAgentString,
            DEFAULT_TOKEN,
            tokenStatus,
            remainingRequests,
            now,
            null,
            now,
            now
        );
    }
}



