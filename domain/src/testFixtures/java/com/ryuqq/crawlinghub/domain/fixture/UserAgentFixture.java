package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.vo.UserAgentId;
import com.ryuqq.crawlinghub.domain.vo.UserAgentStatus;

import java.time.Clock;

/**
 * UserAgent 관련 테스트 데이터 생성 Fixture
 *
 * <p>UserAgent Aggregate와 관련된 Value Object, Enum의 기본값을 제공합니다.</p>
 *
 * <p>표준 패턴 준수:</p>
 * <ul>
 *   <li>{@link #forNew()} - 새 UserAgent 생성 (ID 자동 생성)</li>
 *   <li>{@link #of(String)} - 불변 속성으로 재구성</li>
 *   <li>{@link #reconstitute(UserAgentId, String, String, UserAgentStatus, Integer)} - 완전한 재구성</li>
 * </ul>
 *
 * <p>레거시 호환 메서드:</p>
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
     * 새로운 UserAgent 생성 (표준 패턴)
     *
     * <p>forNew() 패턴: 신규 엔티티 생성</p>
     * <ul>
     *   <li>초기 상태: ACTIVE</li>
     *   <li>초기 requestCount: 0</li>
     *   <li>token: null</li>
     * </ul>
     *
     * @return 새로 생성된 UserAgent
     */
    public static UserAgent forNew() {
        return UserAgent.forNew(DEFAULT_USER_AGENT_STRING);
    }

    /**
     * 새로운 UserAgent 생성 (Clock 주입 - 테스트용)
     *
     * @param clock 시간 제어를 위한 Clock
     * @return 새로 생성된 UserAgent
     */
    public static UserAgent forNew(Clock clock) {
        // TODO: UserAgent가 Clock 파라미터를 지원하면 호출
        return UserAgent.forNew(DEFAULT_USER_AGENT_STRING);
    }

    /**
     * 불변 속성으로 UserAgent 재구성 (표준 패턴)
     *
     * <p>of() 패턴: 테스트용 간편 생성</p>
     * <ul>
     *   <li>초기 상태: ACTIVE</li>
     *   <li>초기 requestCount: 0</li>
     *   <li>token: null</li>
     * </ul>
     *
     * @param userAgentString User Agent 문자열
     * @return 재구성된 UserAgent
     */
    public static UserAgent of(String userAgentString) {
        return UserAgent.of(userAgentString);
    }

    /**
     * 불변 속성으로 UserAgent 재구성 (Clock 주입 - 테스트용)
     *
     * @param userAgentString User Agent 문자열
     * @param clock 시간 제어를 위한 Clock
     * @return 재구성된 UserAgent
     */
    public static UserAgent of(String userAgentString, Clock clock) {
        // TODO: UserAgent가 Clock 파라미터를 지원하면 호출
        return UserAgent.of(userAgentString);
    }

    /**
     * 완전한 UserAgent 재구성 (표준 패턴)
     *
     * <p>reconstitute() 패턴: DB에서 조회한 엔티티 재구성</p>
     *
     * @param userAgentId UserAgent ID
     * @param userAgentString User Agent 문자열
     * @param token 토큰 (nullable)
     * @param status 상태
     * @param requestCount 요청 횟수
     * @return 재구성된 UserAgent
     */
    public static UserAgent reconstitute(UserAgentId userAgentId, String userAgentString,
                                          String token, UserAgentStatus status, Integer requestCount) {
        return UserAgent.reconstitute(userAgentId, userAgentString, token, status, requestCount);
    }

    /**
     * 완전한 UserAgent 재구성 (Clock 주입 - 테스트용)
     *
     * @param userAgentId UserAgent ID
     * @param userAgentString User Agent 문자열
     * @param token 토큰 (nullable)
     * @param status 상태
     * @param requestCount 요청 횟수
     * @param clock 시간 제어를 위한 Clock
     * @return 재구성된 UserAgent
     */
    public static UserAgent reconstitute(UserAgentId userAgentId, String userAgentString,
                                          String token, UserAgentStatus status, Integer requestCount, Clock clock) {
        // TODO: UserAgent가 Clock 파라미터를 지원하면 호출
        return UserAgent.reconstitute(userAgentId, userAgentString, token, status, requestCount);
    }

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
