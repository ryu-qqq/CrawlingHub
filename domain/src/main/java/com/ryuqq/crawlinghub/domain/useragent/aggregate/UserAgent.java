package com.ryuqq.crawlinghub.domain.useragent.aggregate;

import com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentStateException;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.CooldownPolicy;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;
import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentMetadata;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentString;
import java.time.Instant;

/**
 * UserAgent Aggregate Root (HikariCP 벤치마킹)
 *
 * <p>크롤러 UserAgent의 핵심 비즈니스 규칙과 불변식을 관리하는 Aggregate Root
 *
 * <p><strong>상태 전환 규칙</strong>:
 *
 * <pre>
 * IDLE ←→ BORROWED (borrow/return)
 *   ↓
 * COOLDOWN (429 Graduated Backoff)
 *   ↓
 * SUSPENDED (연속 429 또는 Health &lt; 30)
 * </pre>
 *
 * <p><strong>Health Score 규칙</strong>:
 *
 * <ul>
 *   <li>초기값: 100
 *   <li>성공 시: +5 (최대 100)
 *   <li>429 응답: -20, COOLDOWN/SUSPENDED
 *   <li>5xx 응답: -10
 *   <li>기타 실패: -5
 *   <li>Health Score &lt; 30: 자동 SUSPENDED
 *   <li>복구 시: Health Score 70
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class UserAgent {

    private final UserAgentId id;
    private Token token;
    private final UserAgentString userAgentString;
    private final DeviceType deviceType;
    private final UserAgentMetadata metadata;
    private UserAgentStatus status;
    private HealthScore healthScore;
    private CooldownPolicy cooldownPolicy;
    private Instant lastUsedAt;
    private int requestsPerDay;
    private final Instant createdAt;
    private Instant updatedAt;

    private UserAgent(
            UserAgentId id,
            Token token,
            UserAgentString userAgentString,
            DeviceType deviceType,
            UserAgentMetadata metadata,
            UserAgentStatus status,
            HealthScore healthScore,
            CooldownPolicy cooldownPolicy,
            Instant lastUsedAt,
            int requestsPerDay,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.token = token;
        this.userAgentString = userAgentString;
        this.deviceType = deviceType;
        this.metadata = metadata;
        this.status = status;
        this.healthScore = healthScore;
        this.cooldownPolicy = cooldownPolicy != null ? cooldownPolicy : CooldownPolicy.none();
        this.lastUsedAt = lastUsedAt;
        this.requestsPerDay = requestsPerDay;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 UserAgent 생성
     *
     * @param token 암호화된 토큰
     * @param userAgentString User-Agent 헤더 문자열
     * @param now 현재 시각
     * @return 새로운 UserAgent (IDLE, Health Score 100)
     */
    public static UserAgent forNew(Token token, UserAgentString userAgentString, Instant now) {
        DeviceType deviceType = DeviceType.parse(userAgentString.value());
        UserAgentMetadata metadata = UserAgentMetadata.parseFrom(userAgentString.value());
        return new UserAgent(
                UserAgentId.forNew(),
                token,
                userAgentString,
                deviceType,
                metadata,
                UserAgentStatus.IDLE,
                HealthScore.initial(),
                CooldownPolicy.none(),
                null,
                0,
                now,
                now);
    }

    /**
     * 토큰 없이 신규 UserAgent 생성 (Lazy Token Issuance)
     *
     * @param userAgentString User-Agent 헤더 문자열
     * @param now 현재 시각
     * @return 토큰이 없는 새로운 UserAgent (IDLE, Health Score 100)
     */
    public static UserAgent forNewWithoutToken(UserAgentString userAgentString, Instant now) {
        DeviceType deviceType = DeviceType.parse(userAgentString.value());
        UserAgentMetadata metadata = UserAgentMetadata.parseFrom(userAgentString.value());
        return new UserAgent(
                UserAgentId.forNew(),
                Token.empty(),
                userAgentString,
                deviceType,
                metadata,
                UserAgentStatus.IDLE,
                HealthScore.initial(),
                CooldownPolicy.none(),
                null,
                0,
                now,
                now);
    }

    /**
     * 기존 데이터로 UserAgent 복원 (영속성 계층 전용)
     *
     * @param id UserAgent ID
     * @param token 암호화된 토큰
     * @param userAgentString User-Agent 헤더 문자열
     * @param deviceType 디바이스 타입
     * @param metadata User-Agent 메타데이터
     * @param status 현재 상태
     * @param healthScore 건강 점수
     * @param cooldownPolicy 쿨다운 정책
     * @param lastUsedAt 마지막 사용 시각
     * @param requestsPerDay 일일 요청 수
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return 복원된 UserAgent
     */
    public static UserAgent reconstitute(
            UserAgentId id,
            Token token,
            UserAgentString userAgentString,
            DeviceType deviceType,
            UserAgentMetadata metadata,
            UserAgentStatus status,
            HealthScore healthScore,
            CooldownPolicy cooldownPolicy,
            Instant lastUsedAt,
            int requestsPerDay,
            Instant createdAt,
            Instant updatedAt) {
        return new UserAgent(
                id,
                token,
                userAgentString,
                deviceType,
                metadata,
                status,
                healthScore,
                cooldownPolicy,
                lastUsedAt,
                requestsPerDay,
                createdAt,
                updatedAt);
    }

    /**
     * 기존 데이터로 UserAgent 복원 (cooldownPolicy 없는 버전, 하위 호환)
     *
     * @param id UserAgent ID
     * @param token 암호화된 토큰
     * @param userAgentString User-Agent 헤더 문자열
     * @param deviceType 디바이스 타입
     * @param metadata User-Agent 메타데이터
     * @param status 현재 상태
     * @param healthScore 건강 점수
     * @param lastUsedAt 마지막 사용 시각
     * @param requestsPerDay 일일 요청 수
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return 복원된 UserAgent
     */
    public static UserAgent reconstitute(
            UserAgentId id,
            Token token,
            UserAgentString userAgentString,
            DeviceType deviceType,
            UserAgentMetadata metadata,
            UserAgentStatus status,
            HealthScore healthScore,
            Instant lastUsedAt,
            int requestsPerDay,
            Instant createdAt,
            Instant updatedAt) {
        return new UserAgent(
                id,
                token,
                userAgentString,
                deviceType,
                metadata,
                status,
                healthScore,
                CooldownPolicy.none(),
                lastUsedAt,
                requestsPerDay,
                createdAt,
                updatedAt);
    }

    // === Borrow/Return 비즈니스 로직 (HikariCP 패턴) ===

    /**
     * IDLE → BORROWED (HikariCP getConnection 대응)
     *
     * @param now 현재 시각
     * @throws InvalidUserAgentStateException IDLE이 아닌 경우
     */
    public void markBorrowed(Instant now) {
        validateState(UserAgentStatus.IDLE, "borrow");
        this.status = UserAgentStatus.BORROWED;
        this.lastUsedAt = now;
        this.requestsPerDay++;
        this.updatedAt = now;
    }

    /**
     * BORROWED → IDLE (성공 반납, HikariCP connection.close 대응)
     *
     * @param now 현재 시각
     */
    public void returnSuccess(Instant now) {
        validateState(UserAgentStatus.BORROWED, "return");
        this.healthScore = this.healthScore.recordSuccess();
        this.cooldownPolicy = CooldownPolicy.none();
        this.status = UserAgentStatus.IDLE;
        this.lastUsedAt = now;
        this.updatedAt = now;
    }

    /**
     * BORROWED → COOLDOWN/SUSPENDED/IDLE (실패 반납)
     *
     * <ul>
     *   <li>429: COOLDOWN (Graduated Backoff), 연속 5회 시 SUSPENDED
     *   <li>Health &lt; threshold: SUSPENDED
     *   <li>경미한 실패: IDLE 복귀
     * </ul>
     *
     * @param httpStatusCode HTTP 상태 코드
     * @param now 현재 시각
     */
    public void returnWithCooldown(int httpStatusCode, Instant now) {
        validateState(UserAgentStatus.BORROWED, "return");
        this.healthScore = this.healthScore.applyPenalty(httpStatusCode);

        if (httpStatusCode == HealthScore.RATE_LIMIT_STATUS_CODE) {
            this.cooldownPolicy =
                    CooldownPolicy.escalate(this.cooldownPolicy.consecutiveRateLimits(), now);

            if (this.cooldownPolicy.shouldEscalateToSuspended()) {
                this.status = UserAgentStatus.SUSPENDED;
            } else {
                this.status = UserAgentStatus.COOLDOWN;
            }
        } else if (this.healthScore.isBelowSuspensionThreshold()) {
            this.status = UserAgentStatus.SUSPENDED;
        } else {
            this.status = UserAgentStatus.IDLE;
        }
        this.lastUsedAt = now;
        this.updatedAt = now;
    }

    /**
     * COOLDOWN → IDLE 또는 SESSION_REQUIRED (쿨다운 만료 후 자동 복구)
     *
     * @param now 현재 시각
     * @param hasValidSession 세션이 유효한지 여부
     */
    public void recoverFromCooldown(Instant now, boolean hasValidSession) {
        validateState(UserAgentStatus.COOLDOWN, "recoverFromCooldown");
        this.status = hasValidSession ? UserAgentStatus.IDLE : UserAgentStatus.SESSION_REQUIRED;
        this.updatedAt = now;
    }

    // === 기존 비즈니스 로직 (호환 유지) ===

    /**
     * 토큰 발급 (Lazy Token Issuance)
     *
     * @param token 발급할 토큰
     * @param now 현재 시각
     */
    public void issueToken(Token token, Instant now) {
        if (hasToken()) {
            throw new IllegalStateException("이미 토큰이 발급되었습니다: userAgentId=" + this.id.value());
        }
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("발급할 토큰은 비어있을 수 없습니다.");
        }
        this.token = token;
        this.updatedAt = now;
    }

    /**
     * 토큰 발급 여부 확인
     *
     * @return 토큰이 발급되었으면 true
     */
    public boolean hasToken() {
        return this.token != null && this.token.isPresent();
    }

    /**
     * 사용 기록 (lastUsedAt, requestsPerDay 업데이트)
     *
     * @param now 현재 시각
     */
    public void markAsUsed(Instant now) {
        this.lastUsedAt = now;
        this.requestsPerDay++;
        this.updatedAt = now;
    }

    /**
     * 성공 기록 (Health Score +5, 최대 100)
     *
     * @param now 현재 시각
     */
    public void recordSuccess(Instant now) {
        this.healthScore = this.healthScore.recordSuccess();
        this.cooldownPolicy = CooldownPolicy.none();
        this.lastUsedAt = now;
        this.updatedAt = now;
    }

    /**
     * 실패 기록 (HTTP 상태 코드 기반)
     *
     * @param httpStatusCode HTTP 응답 상태 코드
     * @param now 현재 시각
     */
    public void recordFailure(int httpStatusCode, Instant now) {
        if (httpStatusCode == HealthScore.RATE_LIMIT_STATUS_CODE) {
            this.healthScore = this.healthScore.recordRateLimitFailure();
            this.cooldownPolicy =
                    CooldownPolicy.escalate(this.cooldownPolicy.consecutiveRateLimits(), now);
            if (this.cooldownPolicy.shouldEscalateToSuspended()) {
                this.status = UserAgentStatus.SUSPENDED;
            } else {
                this.status = UserAgentStatus.COOLDOWN;
            }
        } else if (httpStatusCode >= 500) {
            this.healthScore = this.healthScore.recordServerError();
            checkAndSuspend();
        } else {
            this.healthScore = this.healthScore.recordOtherError();
            checkAndSuspend();
        }

        this.lastUsedAt = now;
        this.updatedAt = now;
    }

    /** Health Score < 30이면 자동 SUSPENDED */
    private void checkAndSuspend() {
        if (this.healthScore.isBelowSuspensionThreshold() && this.status.isAvailable()) {
            this.status = UserAgentStatus.SUSPENDED;
        }
    }

    /**
     * 수동 정지
     *
     * @param now 현재 시각
     * @throws InvalidUserAgentStateException 현재 상태가 활성이 아닌 경우
     */
    public void suspend(Instant now) {
        if (!this.status.isAvailable()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.SUSPENDED);
        }
        this.status = UserAgentStatus.SUSPENDED;
        this.updatedAt = now;
    }

    /**
     * 복구 (SUSPENDED → IDLE, Health Score 70)
     *
     * @param now 현재 시각
     * @throws InvalidUserAgentStateException 복구 불가능한 상태인 경우
     */
    public void recover(Instant now) {
        if (!this.status.canRecover()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.IDLE);
        }
        this.status = UserAgentStatus.IDLE;
        this.healthScore = HealthScore.recovered();
        this.cooldownPolicy = CooldownPolicy.none();
        this.updatedAt = now;
    }

    /**
     * 영구 차단
     *
     * @param now 현재 시각
     * @throws InvalidUserAgentStateException 이미 BLOCKED 상태인 경우
     */
    public void block(Instant now) {
        if (this.status.isBlocked()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.BLOCKED);
        }
        this.status = UserAgentStatus.BLOCKED;
        this.updatedAt = now;
    }

    /**
     * 차단 해제 (BLOCKED → IDLE, Health Score 70)
     *
     * @param now 현재 시각
     * @throws InvalidUserAgentStateException BLOCKED 상태가 아닌 경우
     */
    public void unblock(Instant now) {
        if (!this.status.isBlocked()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.IDLE);
        }
        this.status = UserAgentStatus.IDLE;
        this.healthScore = HealthScore.recovered();
        this.cooldownPolicy = CooldownPolicy.none();
        this.updatedAt = now;
    }

    /**
     * 상태 변경 (관리자용 배치 처리)
     *
     * @param newStatus 변경할 상태
     * @param now 현재 시각
     * @throws InvalidUserAgentStateException 동일한 상태로 변경하려는 경우
     */
    public void changeStatus(UserAgentStatus newStatus, Instant now) {
        if (this.status == newStatus) {
            throw new InvalidUserAgentStateException(this.status, newStatus);
        }

        if (newStatus.isAvailable()) {
            this.healthScore = HealthScore.recovered();
            this.cooldownPolicy = CooldownPolicy.none();
        }

        this.status = newStatus;
        this.updatedAt = now;
    }

    /**
     * 일일 요청 수 초기화
     *
     * @param now 현재 시각
     */
    public void resetDailyRequests(Instant now) {
        this.requestsPerDay = 0;
        this.updatedAt = now;
    }

    /**
     * Health Score 초기화 (100으로 리셋)
     *
     * @param now 현재 시각
     */
    public void resetHealth(Instant now) {
        this.healthScore = HealthScore.initial();
        this.cooldownPolicy = CooldownPolicy.none();
        this.updatedAt = now;
    }

    // === 상태 확인 메서드 ===

    public boolean isAvailable() {
        return this.status.isAvailable();
    }

    public boolean isSuspended() {
        return this.status == UserAgentStatus.SUSPENDED;
    }

    public boolean isBlocked() {
        return this.status.isBlocked();
    }

    /**
     * 복구 대상인지 확인
     *
     * @param threshold 복구 기준 시각
     * @return 복구 대상이면 true
     */
    public boolean isRecoverable(Instant threshold) {
        return this.status.canRecover()
                && this.lastUsedAt != null
                && this.lastUsedAt.isBefore(threshold);
    }

    // === Private helpers ===

    private void validateState(UserAgentStatus expected, String operation) {
        if (this.status != expected) {
            throw new InvalidUserAgentStateException(this.status, expected);
        }
    }

    // === Getters ===

    public UserAgentId getId() {
        return id;
    }

    public Long getIdValue() {
        return id.value();
    }

    public Token getToken() {
        return token;
    }

    public String getTokenValue() {
        return token != null ? token.encryptedValue() : null;
    }

    public UserAgentString getUserAgentString() {
        return userAgentString;
    }

    public String getUserAgentStringValue() {
        return userAgentString.value();
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public UserAgentMetadata getMetadata() {
        return metadata;
    }

    public UserAgentStatus getStatus() {
        return status;
    }

    public HealthScore getHealthScore() {
        return healthScore;
    }

    public int getHealthScoreValue() {
        return healthScore.value();
    }

    public CooldownPolicy getCooldownPolicy() {
        return cooldownPolicy;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public int getRequestsPerDay() {
        return requestsPerDay;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
