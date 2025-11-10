package com.ryuqq.crawlinghub.domain.useragent;

import com.ryuqq.crawlinghub.domain.token.Token;
import com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentException;
import com.ryuqq.crawlinghub.domain.useragent.exception.RateLimitExceededException;
import com.ryuqq.crawlinghub.domain.useragent.exception.TokenExpiredException;

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
    private Token currentToken;  // ⭐ Token VO 사용
    private TokenStatus tokenStatus;
    private Integer remainingRequests;
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
        Token currentToken,
        TokenStatus tokenStatus,
        Integer remainingRequests,
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
     * DB reconstitute (Token VO 사용)
     */
    public static UserAgent reconstitute(
        UserAgentId id,
        String userAgentString,
        Token currentToken,
        TokenStatus tokenStatus,
        Integer remainingRequests,
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
            rateLimitResetAt,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    private static void validateRequiredFields(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            throw new InvalidUserAgentException(userAgentString);
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
            if (isTokenExpired()) {
                throw new TokenExpiredException(getIdValue());
            }
            if (remainingRequests <= 0) {
                throw new RateLimitExceededException(getIdValue(), remainingRequests);
            }
            throw new IllegalStateException("요청을 수행할 수 없는 상태입니다");
        }
        this.remainingRequests--;
        this.tokenStatus = TokenStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 새 토큰 발급 (Token VO 사용)
     */
    public void issueNewToken(Token token) {
        if (token == null) {
            throw new IllegalArgumentException("토큰은 필수입니다");
        }
        this.currentToken = token;
        this.tokenStatus = TokenStatus.IDLE;
        this.remainingRequests = MAX_REQUESTS_PER_HOUR;
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
     * 토큰 만료 여부 확인 (Token VO 활용)
     */
    public boolean isTokenExpired() {
        if (currentToken == null) {
            return true;
        }
        return currentToken.isExpired(LocalDateTime.now(clock));
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

    public Token getCurrentToken() {
        return currentToken;
    }

    public TokenStatus getTokenStatus() {
        return tokenStatus;
    }

    public Integer getRemainingRequests() {
        return remainingRequests;
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
