package com.ryuqq.crawlinghub.domain.useragent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 유저 에이전트 Aggregate Root
 * 
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>시간당 최대 80회 요청</li>
 *   <li>429 응답 시 즉시 토큰 폐기</li>
 *   <li>토큰 유효기간: 24시간</li>
 *   <li>DISABLED 상태 1시간 후 자동 RECOVERED</li>
 * </ul>
 */
public class UserAgent {

    private static final int MAX_REQUESTS_PER_HOUR = 80;
    private static final int TOKEN_VALIDITY_HOURS = 24;
    private static final int RECOVERY_HOURS = 1;

    private final UserAgentId id;
    private final String userAgentString;
    private String currentToken;
    private TokenStatus tokenStatus;
    private Integer remainingRequests;
    private LocalDateTime tokenIssuedAt;
    private LocalDateTime rateLimitResetAt;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private UserAgent(
        UserAgentId id,
        String userAgentString,
        String currentToken,
        TokenStatus tokenStatus,
        Integer remainingRequests,
        LocalDateTime tokenIssuedAt,
        LocalDateTime rateLimitResetAt,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userAgentString = userAgentString;
        this.currentToken = currentToken;
        this.tokenStatus = tokenStatus;
        this.remainingRequests = remainingRequests;
        this.tokenIssuedAt = tokenIssuedAt;
        this.rateLimitResetAt = rateLimitResetAt;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Package-private 주요 생성자 (검증 포함)
     */
    UserAgent(
        UserAgentId id,
        String userAgentString,
        Clock clock
    ) {
        validateRequiredFields(userAgentString);

        this.id = id;
        this.userAgentString = userAgentString;
        this.currentToken = null;
        this.tokenStatus = TokenStatus.IDLE;
        this.remainingRequests = MAX_REQUESTS_PER_HOUR;
        this.tokenIssuedAt = null;
        this.rateLimitResetAt = null;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 신규 유저 에이전트 생성 (ID 없음)
     */
    public static UserAgent forNew(String userAgentString) {
        return new UserAgent(null, userAgentString, Clock.systemDefaultZone());
    }

    /**
     * 기존 유저 에이전트 생성 (ID 있음)
     */
    public static UserAgent of(UserAgentId id, String userAgentString) {
        if (id == null) {
            throw new IllegalArgumentException("UserAgent ID는 필수입니다");
        }
        return new UserAgent(id, userAgentString, Clock.systemDefaultZone());
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static UserAgent reconstitute(
        UserAgentId id,
        String userAgentString,
        String currentToken,
        TokenStatus tokenStatus,
        Integer remainingRequests,
        LocalDateTime tokenIssuedAt,
        LocalDateTime rateLimitResetAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new UserAgent(
            id,
            userAgentString,
            currentToken,
            tokenStatus,
            remainingRequests,
            tokenIssuedAt,
            rateLimitResetAt,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    private static void validateRequiredFields(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            throw new IllegalArgumentException("User Agent 문자열은 필수입니다");
        }
    }

    /**
     * 요청 가능 여부 확인
     */
    public boolean canMakeRequest() {
        if (!tokenStatus.canMakeRequest()) {
            return false;
        }
        if (isTokenExpired()) {
            return false;
        }
        return remainingRequests > 0;
    }

    /**
     * 요청 소비
     */
    public void consumeRequest() {
        if (!canMakeRequest()) {
            throw new IllegalStateException("요청을 수행할 수 없는 상태입니다");
        }
        this.remainingRequests--;
        this.tokenStatus = TokenStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 새 토큰 발급
     */
    public void issueNewToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("토큰은 필수입니다");
        }
        this.currentToken = token;
        this.tokenStatus = TokenStatus.IDLE;
        this.remainingRequests = MAX_REQUESTS_PER_HOUR;
        this.tokenIssuedAt = LocalDateTime.now(clock);
        this.rateLimitResetAt = null;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Rate Limit 에러 처리
     */
    public void handleRateLimitError() {
        this.tokenStatus = TokenStatus.RATE_LIMITED;
        this.currentToken = null;
        this.remainingRequests = 0;
        this.rateLimitResetAt = LocalDateTime.now(clock).plusHours(RECOVERY_HOURS);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Rate Limit 복구
     */
    public void recoverFromRateLimit() {
        if (!tokenStatus.isRateLimited()) {
            throw new IllegalStateException("RATE_LIMITED 상태에서만 복구할 수 있습니다");
        }
        this.tokenStatus = TokenStatus.RECOVERED;
        this.remainingRequests = MAX_REQUESTS_PER_HOUR;
        this.rateLimitResetAt = null;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired() {
        if (tokenIssuedAt == null) {
            return true;
        }
        LocalDateTime expiryTime = tokenIssuedAt.plusHours(TOKEN_VALIDITY_HOURS);
        return LocalDateTime.now(clock).isAfter(expiryTime);
    }

    /**
     * 복구 가능 여부 확인
     */
    public boolean canRecover() {
        if (rateLimitResetAt == null) {
            return false;
        }
        return LocalDateTime.now(clock).isAfter(rateLimitResetAt);
    }

    /**
     * 비활성화
     */
    public void disable() {
        this.tokenStatus = TokenStatus.DISABLED;
        this.currentToken = null;
        this.remainingRequests = 0;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 특정 상태인지 확인
     */
    public boolean hasStatus(TokenStatus targetStatus) {
        return this.tokenStatus == targetStatus;
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public String getUserAgentString() {
        return userAgentString;
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public TokenStatus getTokenStatus() {
        return tokenStatus;
    }

    public Integer getRemainingRequests() {
        return remainingRequests;
    }

    public LocalDateTime getTokenIssuedAt() {
        return tokenIssuedAt;
    }

    public LocalDateTime getRateLimitResetAt() {
        return rateLimitResetAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAgent that = (UserAgent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserAgent{" +
            "id=" + id +
            ", userAgentString='" + userAgentString + '\'' +
            ", tokenStatus=" + tokenStatus +
            ", remainingRequests=" + remainingRequests +
            '}';
    }
}
