package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.UserAgentId;
import com.ryuqq.crawlinghub.domain.vo.UserAgentStatus;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * UserAgent Aggregate Root
 *
 * <p>머스트잇 크롤링에 사용되는 User Agent를 관리하는 Aggregate Root입니다.</p>
 *
 * <p>Zero-Tolerance Rules 준수:</p>
 * <ul>
 *   <li>Lombok 금지 - Plain Java 사용</li>
 *   <li>Tell, Don't Ask - 비즈니스 로직은 UserAgent 내부에 캡슐화</li>
 *   <li>Long FK 전략 - JPA 관계 어노테이션 없음</li>
 * </ul>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>생성 시 상태는 항상 ACTIVE</li>
 *   <li>생성 시 requestCount는 0</li>
 *   <li>token은 생성 시점에는 null (별도 발급 필요)</li>
 *   <li>시간당 최대 80회 요청 제한 (Token Bucket Rate Limiter)</li>
 * </ul>
 */
public class UserAgent {

    private static final int MAX_REQUESTS_PER_HOUR = 80;

    private final UserAgentId userAgentId;
    private final String userAgentString;
    private final Clock clock;
    private String token;
    private UserAgentStatus status;
    private Integer requestCount;
    private LocalDateTime lastRequestAt;
    private LocalDateTime tokenIssuedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성 (신규)
     *
     * @param userAgentString User Agent 문자열
     * @param clock 시간 제어를 위한 Clock
     */
    private UserAgent(String userAgentString, Clock clock) {
        if (userAgentString == null || userAgentString.isBlank()) {
            throw new IllegalArgumentException("UserAgent 문자열은 비어있을 수 없습니다");
        }
        this.userAgentId = UserAgentId.generate();
        this.userAgentString = userAgentString;
        this.clock = clock;
        this.status = UserAgentStatus.ACTIVE;
        this.requestCount = 0;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성 (재구성)
     *
     * @param userAgentId UserAgent ID
     * @param userAgentString User Agent 문자열
     * @param token 토큰 (nullable)
     * @param status 상태
     * @param requestCount 요청 횟수
     * @param clock 시간 제어를 위한 Clock
     */
    private UserAgent(UserAgentId userAgentId, String userAgentString, String token,
                       UserAgentStatus status, Integer requestCount, Clock clock) {
        if (userAgentString == null || userAgentString.isBlank()) {
            throw new IllegalArgumentException("UserAgent 문자열은 비어있을 수 없습니다");
        }
        this.userAgentId = userAgentId;
        this.userAgentString = userAgentString;
        this.clock = clock;
        this.token = token;
        this.status = status;
        this.requestCount = requestCount;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 새로운 UserAgent 생성 (표준 패턴)
     *
     * <p>forNew() 패턴: 신규 엔티티 생성</p>
     * <ul>
     *   <li>초기 상태: ACTIVE</li>
     *   <li>초기 requestCount: 0</li>
     *   <li>token은 null (별도 발급 필요)</li>
     * </ul>
     *
     * @param userAgentString User Agent 문자열
     * @return 새로 생성된 UserAgent
     * @throws IllegalArgumentException userAgentString이 null 또는 blank인 경우
     */
    public static UserAgent forNew(String userAgentString) {
        return forNew(userAgentString, Clock.systemDefaultZone());
    }

    /**
     * 새로운 UserAgent 생성 (Clock 주입)
     *
     * @param userAgentString User Agent 문자열
     * @param clock 시간 제어를 위한 Clock
     * @return 새로 생성된 UserAgent
     * @throws IllegalArgumentException userAgentString이 null 또는 blank인 경우
     */
    public static UserAgent forNew(String userAgentString, Clock clock) {
        return new UserAgent(userAgentString, clock);
    }

    /**
     * 불변 속성으로 UserAgent 재구성 (표준 패턴)
     *
     * <p>of() 패턴: 테스트용 간편 생성</p>
     * <ul>
     *   <li>초기 상태: ACTIVE</li>
     *   <li>초기 requestCount: 0</li>
     *   <li>token은 null (별도 발급 필요)</li>
     * </ul>
     *
     * @param userAgentString User Agent 문자열
     * @return 재구성된 UserAgent
     * @throws IllegalArgumentException userAgentString이 null 또는 blank인 경우
     */
    public static UserAgent of(String userAgentString) {
        return of(userAgentString, Clock.systemDefaultZone());
    }

    /**
     * 불변 속성으로 UserAgent 재구성 (Clock 주입)
     *
     * @param userAgentString User Agent 문자열
     * @param clock 시간 제어를 위한 Clock
     * @return 재구성된 UserAgent
     * @throws IllegalArgumentException userAgentString이 null 또는 blank인 경우
     */
    public static UserAgent of(String userAgentString, Clock clock) {
        return new UserAgent(userAgentString, clock);
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
     * @throws IllegalArgumentException userAgentString이 null 또는 blank인 경우
     */
    public static UserAgent reconstitute(UserAgentId userAgentId, String userAgentString, String token,
                                          UserAgentStatus status, Integer requestCount) {
        return reconstitute(userAgentId, userAgentString, token, status, requestCount, Clock.systemDefaultZone());
    }

    /**
     * 완전한 UserAgent 재구성 (Clock 주입)
     *
     * @param userAgentId UserAgent ID
     * @param userAgentString User Agent 문자열
     * @param token 토큰 (nullable)
     * @param status 상태
     * @param requestCount 요청 횟수
     * @param clock 시간 제어를 위한 Clock
     * @return 재구성된 UserAgent
     * @throws IllegalArgumentException userAgentString이 null 또는 blank인 경우
     */
    public static UserAgent reconstitute(UserAgentId userAgentId, String userAgentString, String token,
                                          UserAgentStatus status, Integer requestCount, Clock clock) {
        return new UserAgent(userAgentId, userAgentString, token, status, requestCount, clock);
    }

    /**
     * 새로운 UserAgent 생성 (레거시)
     *
     * @deprecated Use {@link #forNew(String)} instead
     * @param userAgentString User Agent 문자열
     * @return 새로 생성된 UserAgent
     * @throws IllegalArgumentException userAgentString이 null 또는 blank인 경우
     */
    @Deprecated
    public static UserAgent create(String userAgentString) {
        return forNew(userAgentString);
    }

    /**
     * 토큰 발급
     *
     * <p>머스트잇 API 인증 토큰을 발급받아 저장합니다.</p>
     *
     * @param token 발급받은 토큰
     * @throws IllegalArgumentException token이 null 또는 blank인 경우
     */
    public void issueToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("토큰은 비어있을 수 없습니다");
        }
        this.token = token;
        this.tokenIssuedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 요청 가능 여부 확인 (Tell Don't Ask 패턴)
     *
     * <p>Token Bucket Rate Limiter 알고리즘:</p>
     * <ul>
     *   <li>토큰이 없으면 요청 불가</li>
     *   <li>시간당 최대 80회 요청 제한</li>
     *   <li>1시간 경과 시 requestCount 자동 리셋</li>
     * </ul>
     *
     * @return 요청 가능 시 true, 불가능 시 false
     */
    public boolean canMakeRequest() {
        if (token == null) {
            return false;
        }

        LocalDateTime oneHourAgo = LocalDateTime.now(clock).minusHours(1);

        // 1시간 경과 시 requestCount 리셋
        if (lastRequestAt != null && lastRequestAt.isBefore(oneHourAgo)) {
            this.requestCount = 0;
        }

        return requestCount < MAX_REQUESTS_PER_HOUR;
    }

    /**
     * 요청 횟수 증가
     *
     * <p>API 요청을 실행할 때마다 호출하여 요청 횟수를 증가시킵니다.</p>
     */
    public void incrementRequestCount() {
        this.requestCount++;
        this.lastRequestAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * UserAgent 일시 정지
     *
     * <p>429 Too Many Requests 응답 등으로 인해 UserAgent를 일시 정지합니다.</p>
     */
    public void suspend() {
        this.status = UserAgentStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * UserAgent 활성화
     *
     * <p>일시 정지된 UserAgent를 다시 활성화합니다.</p>
     */
    public void activate() {
        this.status = UserAgentStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
    }

    // Getters (필요한 것만)
    public UserAgentId getUserAgentId() {
        return userAgentId;
    }

    public String getUserAgentString() {
        return userAgentString;
    }

    public String getToken() {
        return token;
    }

    public UserAgentStatus getStatus() {
        return status;
    }

    public Integer getRequestCount() {
        return requestCount;
    }

    public LocalDateTime getTokenIssuedAt() {
        return tokenIssuedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
