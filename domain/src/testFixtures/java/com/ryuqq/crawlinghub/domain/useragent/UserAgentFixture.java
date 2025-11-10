package com.ryuqq.crawlinghub.domain.useragent;

import com.ryuqq.crawlinghub.domain.token.Token;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * UserAgent Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class UserAgentFixture {

    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final String CHROME_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String FIREFOX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0";
    private static final String DEFAULT_TOKEN = "test-token-12345";
    private static final Clock DEFAULT_CLOCK = Clock.fixed(
        Instant.parse("2025-01-01T00:00:00Z"),
        ZoneId.systemDefault()
    );

    /**
     * 기본 UserAgent 생성 (신규)
     *
     * @return UserAgent
     */
    public static UserAgent create() {
        return UserAgent.forNew(DEFAULT_USER_AGENT);
    }

    /**
     * ID를 가진 UserAgent 생성
     *
     * @param id UserAgent ID
     * @return UserAgent
     */
    public static UserAgent createWithId(Long id) {
        return UserAgent.of(UserAgentId.of(id), DEFAULT_USER_AGENT);
    }

    /**
     * 특정 User Agent 문자열로 생성
     *
     * @param userAgentString User Agent 문자열
     * @return UserAgent
     */
    public static UserAgent createWithString(String userAgentString) {
        return UserAgent.forNew(userAgentString);
    }

    /**
     * Chrome User Agent 생성
     *
     * @return UserAgent
     */
    public static UserAgent createChrome() {
        return UserAgent.forNew(CHROME_USER_AGENT);
    }

    /**
     * Firefox User Agent 생성
     *
     * @return UserAgent
     */
    public static UserAgent createFirefox() {
        return UserAgent.forNew(FIREFOX_USER_AGENT);
    }

    /**
     * IDLE 상태의 UserAgent 생성
     *
     * @return UserAgent
     */
    public static UserAgent createIdle() {
        return reconstitute(DEFAULT_ID, TokenStatus.IDLE, 80, null);
    }

    /**
     * ACTIVE 상태의 UserAgent 생성
     *
     * @param remainingRequests 남은 요청 수
     * @return UserAgent
     */
    public static UserAgent createActive(int remainingRequests) {
        LocalDateTime now = LocalDateTime.now();  // 시스템 현재 시간 사용 (isTokenExpired 통과를 위해)
        Token token = Token.of(DEFAULT_TOKEN, now, now.plusHours(24));
        return UserAgent.reconstitute(
            UserAgentId.of(DEFAULT_ID),
            DEFAULT_USER_AGENT,
            token,
            TokenStatus.ACTIVE,
            remainingRequests,
            null,
            now,
            now
        );
    }

    /**
     * RATE_LIMITED 상태의 UserAgent 생성
     *
     * @return UserAgent
     */
    public static UserAgent createRateLimited() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return UserAgent.reconstitute(
            UserAgentId.of(DEFAULT_ID),
            DEFAULT_USER_AGENT,
            null,
            TokenStatus.RATE_LIMITED,
            0,
            now.plusHours(1),
            now,
            now
        );
    }

    /**
     * DISABLED 상태의 UserAgent 생성
     *
     * @return UserAgent
     */
    public static UserAgent createDisabled() {
        return reconstitute(DEFAULT_ID, TokenStatus.DISABLED, 0, null);
    }

    /**
     * RECOVERED 상태의 UserAgent 생성
     *
     * @return UserAgent
     */
    public static UserAgent createRecovered() {
        return reconstitute(DEFAULT_ID, TokenStatus.RECOVERED, 80, null);
    }

    /**
     * 토큰이 발급된 UserAgent 생성
     *
     * @param token 토큰
     * @return UserAgent
     */
    public static UserAgent createWithToken(String token) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        Token tokenVO = Token.of(token, now, now.plusHours(24));
        return UserAgent.reconstitute(
            UserAgentId.of(DEFAULT_ID),
            DEFAULT_USER_AGENT,
            tokenVO,
            TokenStatus.IDLE,
            80,
            null,
            now,
            now
        );
    }

    /**
     * 토큰이 만료된 UserAgent 생성
     *
     * @return UserAgent
     */
    public static UserAgent createWithExpiredToken() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        LocalDateTime expiredTime = now.minusHours(25); // 24시간 + 1시간
        Token token = Token.of(DEFAULT_TOKEN, expiredTime, expiredTime.plusHours(24));
        return UserAgent.reconstitute(
            UserAgentId.of(DEFAULT_ID),
            DEFAULT_USER_AGENT,
            token,
            TokenStatus.IDLE,
            80,
            null,
            now,
            now
        );
    }

    /**
     * 요청 가능한 UserAgent 생성
     *
     * @param remainingRequests 남은 요청 수
     * @return UserAgent
     */
    public static UserAgent createCanMakeRequest(int remainingRequests) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        Token token = Token.of(DEFAULT_TOKEN, now, now.plusHours(24));
        return UserAgent.reconstitute(
            UserAgentId.of(DEFAULT_ID),
            DEFAULT_USER_AGENT,
            token,
            TokenStatus.IDLE,
            remainingRequests,
            null,
            now,
            now
        );
    }

    /**
     * 요청 불가능한 UserAgent 생성 (남은 요청 0)
     *
     * @return UserAgent
     */
    public static UserAgent createCannotMakeRequest() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        Token token = Token.of(DEFAULT_TOKEN, now, now.plusHours(24));
        return UserAgent.reconstitute(
            UserAgentId.of(DEFAULT_ID),
            DEFAULT_USER_AGENT,
            token,
            TokenStatus.ACTIVE,
            0,
            null,
            now,
            now
        );
    }

    /**
     * 복구 가능한 UserAgent 생성
     *
     * @return UserAgent
     */
    public static UserAgent createCanRecover() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        LocalDateTime pastResetTime = now.minusHours(2);
        return UserAgent.reconstitute(
            UserAgentId.of(DEFAULT_ID),
            DEFAULT_USER_AGENT,
            null,
            TokenStatus.RATE_LIMITED,
            0,
            pastResetTime,
            now,
            now
        );
    }

    /**
     * DB reconstitute용 UserAgent 생성
     *
     * @param id UserAgent ID
     * @param status 토큰 상태
     * @param remainingRequests 남은 요청 수
     * @param token 토큰 (nullable)
     * @return UserAgent
     */
    public static UserAgent reconstitute(
        Long id,
        TokenStatus status,
        int remainingRequests,
        String token
    ) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        LocalDateTime tokenIssuedAt = token != null ? now : null;
        LocalDateTime tokenExpiresAt = token != null ? now.plusHours(24) : null;
        LocalDateTime rateLimitResetAt = status == TokenStatus.RATE_LIMITED ? now.plusHours(1) : null;

        Token tokenVO = token != null ? Token.of(token, tokenIssuedAt, tokenExpiresAt) : null;

        return UserAgent.reconstitute(
            UserAgentId.of(id),
            DEFAULT_USER_AGENT,
            tokenVO,
            status,
            remainingRequests,
            rateLimitResetAt,
            now,
            now
        );
    }

    /**
     * 완전한 커스텀 UserAgent 생성
     *
     * @param id UserAgent ID (null 가능)
     * @param userAgentString User Agent 문자열
     * @param token 토큰
     * @param status 토큰 상태
     * @param remainingRequests 남은 요청 수
     * @return UserAgent
     */
    public static UserAgent createCustom(
        Long id,
        String userAgentString,
        String token,
        TokenStatus status,
        int remainingRequests
    ) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        LocalDateTime tokenIssuedAt = token != null ? now : null;
        LocalDateTime tokenExpiresAt = token != null ? now.plusHours(24) : null;
        LocalDateTime rateLimitResetAt = status == TokenStatus.RATE_LIMITED ? now.plusHours(1) : null;

        if (id == null) {
            UserAgent userAgent = UserAgent.forNew(userAgentString);
            if (token != null) {
                Token tokenVO = Token.of(token, tokenIssuedAt, tokenExpiresAt);
                userAgent.issueNewToken(tokenVO);
            }
            return userAgent;
        }

        Token tokenVO = token != null ? Token.of(token, tokenIssuedAt, tokenExpiresAt) : null;

        return UserAgent.reconstitute(
            UserAgentId.of(id),
            userAgentString,
            tokenVO,
            status,
            remainingRequests,
            rateLimitResetAt,
            now,
            now
        );
    }
}
