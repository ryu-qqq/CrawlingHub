package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.vo.UserAgentId;
import com.ryuqq.crawlinghub.domain.vo.UserAgentStatus;

/**
 * UserAgent 관련 테스트 데이터 생성 Fixture
 *
 * <p>UserAgent Aggregate와 관련된 Value Object, Enum의 기본값을 제공합니다.</p>
 *
 * <p>제공 메서드:</p>
 * <ul>
 *   <li>{@link #defaultUserAgent()} - 기본 UserAgent (토큰 없음, ACTIVE)</li>
 *   <li>{@link #userAgentWithToken()} - 토큰이 발급된 UserAgent (ACTIVE)</li>
 *   <li>{@link #defaultUserAgentId()} - 새로운 UserAgentId 생성</li>
 *   <li>{@link #defaultUserAgentStatus()} - 기본 상태 (ACTIVE)</li>
 * </ul>
 */
public class UserAgentFixture {

    private static final String DEFAULT_USER_AGENT_STRING = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final String DEFAULT_TOKEN = "test_token_" + System.currentTimeMillis();

    /**
     * 기본 UserAgent 생성 (토큰 없음, ACTIVE 상태)
     *
     * @return 토큰이 발급되지 않은 ACTIVE 상태의 UserAgent
     */
    public static UserAgent defaultUserAgent() {
        return UserAgent.create(DEFAULT_USER_AGENT_STRING);
    }

    /**
     * 토큰이 발급된 UserAgent 생성 (ACTIVE 상태)
     *
     * @return 토큰이 발급된 ACTIVE 상태의 UserAgent
     */
    public static UserAgent userAgentWithToken() {
        UserAgent userAgent = UserAgent.create(DEFAULT_USER_AGENT_STRING);
        userAgent.issueToken(DEFAULT_TOKEN);
        return userAgent;
    }

    /**
     * 일시 정지된 UserAgent 생성 (SUSPENDED 상태)
     *
     * <p>토큰이 발급된 후 suspend()가 호출된 상태입니다.</p>
     * <p>Note: suspend() 메서드 구현 후 활성화됩니다.</p>
     *
     * @return SUSPENDED 상태의 UserAgent
     */
    // TODO: suspend() 메서드 구현 후 주석 제거
    // public static UserAgent suspendedUserAgent() {
    //     UserAgent userAgent = userAgentWithToken();
    //     userAgent.suspend();
    //     return userAgent;
    // }

    /**
     * 기본 UserAgentId 생성
     *
     * @return 새로운 UUID 기반 UserAgentId
     */
    public static UserAgentId defaultUserAgentId() {
        return UserAgentId.generate();
    }

    /**
     * 기본 UserAgentStatus 반환
     *
     * @return ACTIVE 상태
     */
    public static UserAgentStatus defaultUserAgentStatus() {
        return UserAgentStatus.ACTIVE;
    }
}
