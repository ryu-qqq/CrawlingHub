package com.ryuqq.crawlinghub.domain.useragent.aggregate;

import com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentStateException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;
import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentMetadata;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentString;
import java.time.Clock;
import java.time.Instant;

/**
 * UserAgent Aggregate Root
 *
 * <p>크롤러 UserAgent의 핵심 비즈니스 규칙과 불변식을 관리하는 Aggregate Root
 *
 * <p><strong>상태 전환 규칙</strong>:
 *
 * <pre>
 * AVAILABLE ←→ SUSPENDED (Health Score < 30 또는 429 응답)
 *     ↓              ↓
 *  BLOCKED        1시간 후 자동 복구 (Health Score 70)
 * </pre>
 *
 * <p><strong>Health Score 규칙</strong>:
 *
 * <ul>
 *   <li>초기값: 100
 *   <li>성공 시: +5 (최대 100)
 *   <li>429 응답: -20, SUSPENDED
 *   <li>5xx 응답: -10
 *   <li>기타 실패: -5
 *   <li>Health Score < 30: 자동 SUSPENDED
 *   <li>복구 시: Health Score 70, AVAILABLE
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
        this.lastUsedAt = lastUsedAt;
        this.requestsPerDay = requestsPerDay;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 UserAgent 생성
     *
     * <p>User-Agent 문자열에서 DeviceType과 UserAgentMetadata를 자동으로 파싱합니다.
     *
     * @param token 암호화된 토큰
     * @param userAgentString User-Agent 헤더 문자열
     * @param clock 시간 제어
     * @return 새로운 UserAgent (AVAILABLE, Health Score 100)
     */
    public static UserAgent forNew(Token token, UserAgentString userAgentString, Clock clock) {
        Instant now = clock.instant();
        DeviceType deviceType = DeviceType.parse(userAgentString.value());
        UserAgentMetadata metadata = UserAgentMetadata.parseFrom(userAgentString.value());
        return new UserAgent(
                UserAgentId.unassigned(),
                token,
                userAgentString,
                deviceType,
                metadata,
                UserAgentStatus.AVAILABLE,
                HealthScore.initial(),
                null,
                0,
                now,
                now);
    }

    /**
     * 토큰 없이 신규 UserAgent 생성 (Lazy Token Issuance)
     *
     * <p>토큰 발급을 나중으로 미루고 UserAgent만 먼저 등록할 때 사용합니다. 토큰이 필요한 시점에 {@link #issueToken(Token, Clock)}
     * 메서드로 발급합니다.
     *
     * @param userAgentString User-Agent 헤더 문자열
     * @param clock 시간 제어
     * @return 토큰이 없는 새로운 UserAgent (AVAILABLE, Health Score 100)
     */
    public static UserAgent forNewWithoutToken(UserAgentString userAgentString, Clock clock) {
        Instant now = clock.instant();
        DeviceType deviceType = DeviceType.parse(userAgentString.value());
        UserAgentMetadata metadata = UserAgentMetadata.parseFrom(userAgentString.value());
        return new UserAgent(
                UserAgentId.unassigned(),
                Token.empty(),
                userAgentString,
                deviceType,
                metadata,
                UserAgentStatus.AVAILABLE,
                HealthScore.initial(),
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
     * @param metadata User-Agent 메타데이터 (DeviceBrand, OsType, BrowserType 정보)
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
                lastUsedAt,
                requestsPerDay,
                createdAt,
                updatedAt);
    }

    // === 비즈니스 로직 ===

    /**
     * 토큰 발급 (Lazy Token Issuance)
     *
     * <p>토큰이 없는 UserAgent에 토큰을 발급합니다. 이미 토큰이 발급된 경우 예외가 발생합니다.
     *
     * @param token 발급할 토큰
     * @param clock 시간 제어
     * @throws IllegalStateException 이미 토큰이 발급된 경우
     * @throws IllegalArgumentException 토큰이 비어있는 경우
     */
    public void issueToken(Token token, Clock clock) {
        if (hasToken()) {
            throw new IllegalStateException("이미 토큰이 발급되었습니다: userAgentId=" + this.id.value());
        }
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("발급할 토큰은 비어있을 수 없습니다.");
        }
        this.token = token;
        this.updatedAt = clock.instant();
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
     * @param clock 시간 제어
     */
    public void markAsUsed(Clock clock) {
        Instant now = clock.instant();
        this.lastUsedAt = now;
        this.requestsPerDay++;
        this.updatedAt = now;
    }

    /**
     * 성공 기록 (Health Score +5, 최대 100)
     *
     * @param clock 시간 제어
     */
    public void recordSuccess(Clock clock) {
        Instant now = clock.instant();
        this.healthScore = this.healthScore.recordSuccess();
        this.lastUsedAt = now;
        this.updatedAt = now;
    }

    /**
     * 실패 기록 (HTTP 상태 코드 기반)
     *
     * <ul>
     *   <li>429: Health Score -20, 즉시 SUSPENDED
     *   <li>5xx: Health Score -10
     *   <li>기타: Health Score -5
     * </ul>
     *
     * @param httpStatusCode HTTP 응답 상태 코드
     * @param clock 시간 제어
     */
    public void recordFailure(int httpStatusCode, Clock clock) {
        if (httpStatusCode == 429) {
            this.healthScore = this.healthScore.recordRateLimitFailure();
            this.status = UserAgentStatus.SUSPENDED;
        } else if (httpStatusCode >= 500) {
            this.healthScore = this.healthScore.recordServerError();
            checkAndSuspend();
        } else {
            this.healthScore = this.healthScore.recordOtherError();
            checkAndSuspend();
        }

        Instant now = clock.instant();
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
     * 수동 정지 (AVAILABLE → SUSPENDED)
     *
     * @param clock 시간 제어
     * @throws InvalidUserAgentStateException 현재 상태가 AVAILABLE이 아닌 경우
     */
    public void suspend(Clock clock) {
        if (!this.status.isAvailable()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.SUSPENDED);
        }
        this.status = UserAgentStatus.SUSPENDED;
        this.updatedAt = clock.instant();
    }

    /**
     * 복구 (SUSPENDED → AVAILABLE, Health Score 70)
     *
     * @param clock 시간 제어
     * @throws InvalidUserAgentStateException 복구 불가능한 상태인 경우
     */
    public void recover(Clock clock) {
        if (!this.status.canRecover()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.AVAILABLE);
        }
        this.status = UserAgentStatus.AVAILABLE;
        this.healthScore = HealthScore.recovered();
        this.updatedAt = clock.instant();
    }

    /**
     * 영구 차단 (AVAILABLE/SUSPENDED → BLOCKED)
     *
     * @param clock 시간 제어
     * @throws InvalidUserAgentStateException 이미 BLOCKED 상태인 경우
     */
    public void block(Clock clock) {
        if (this.status.isBlocked()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.BLOCKED);
        }
        this.status = UserAgentStatus.BLOCKED;
        this.updatedAt = clock.instant();
    }

    /**
     * 차단 해제 (BLOCKED → AVAILABLE, Health Score 70)
     *
     * <p>관리자가 차단된 UserAgent를 다시 활성화할 때 사용합니다.
     *
     * @param clock 시간 제어
     * @throws InvalidUserAgentStateException BLOCKED 상태가 아닌 경우
     */
    public void unblock(Clock clock) {
        if (!this.status.isBlocked()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.AVAILABLE);
        }
        this.status = UserAgentStatus.AVAILABLE;
        this.healthScore = HealthScore.recovered();
        this.updatedAt = clock.instant();
    }

    /**
     * 상태 변경 (관리자용 배치 처리)
     *
     * <p>관리자가 명시적으로 상태를 변경할 때 사용합니다. 모든 상태 전환이 가능합니다.
     *
     * @param newStatus 변경할 상태
     * @param clock 시간 제어
     * @throws InvalidUserAgentStateException 동일한 상태로 변경하려는 경우
     */
    public void changeStatus(UserAgentStatus newStatus, Clock clock) {
        if (this.status == newStatus) {
            throw new InvalidUserAgentStateException(this.status, newStatus);
        }

        // AVAILABLE로 변경 시 Health Score 복구
        if (newStatus.isAvailable()) {
            this.healthScore = HealthScore.recovered();
        }

        this.status = newStatus;
        this.updatedAt = clock.instant();
    }

    /**
     * 일일 요청 수 초기화 (매일 자정 실행)
     *
     * @param clock 시간 제어
     */
    public void resetDailyRequests(Clock clock) {
        this.requestsPerDay = 0;
        this.updatedAt = clock.instant();
    }

    /**
     * Health Score 초기화 (100으로 리셋)
     *
     * <p>관리자가 수동으로 Health Score를 초기화할 때 사용합니다.
     *
     * @param clock 시간 제어
     */
    public void resetHealth(Clock clock) {
        this.healthScore = HealthScore.initial();
        this.updatedAt = clock.instant();
    }

    // === 상태 확인 메서드 ===

    /**
     * 사용 가능한 상태인지 확인
     *
     * @return AVAILABLE이면 true
     */
    public boolean isAvailable() {
        return this.status.isAvailable();
    }

    /**
     * 정지 상태인지 확인
     *
     * @return SUSPENDED면 true
     */
    public boolean isSuspended() {
        return this.status == UserAgentStatus.SUSPENDED;
    }

    /**
     * 차단 상태인지 확인
     *
     * @return BLOCKED면 true
     */
    public boolean isBlocked() {
        return this.status.isBlocked();
    }

    /**
     * 복구 대상인지 확인 (SUSPENDED + lastUsedAt < 기준 시각)
     *
     * @param threshold 복구 기준 시각
     * @return 복구 대상이면 true
     */
    public boolean isRecoverable(Instant threshold) {
        return this.status.canRecover()
                && this.lastUsedAt != null
                && this.lastUsedAt.isBefore(threshold);
    }

    // === Getters ===

    public UserAgentId getId() {
        return id;
    }

    public Token getToken() {
        return token;
    }

    public UserAgentString getUserAgentString() {
        return userAgentString;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * User-Agent 메타데이터 반환
     *
     * @return User-Agent 메타데이터 (DeviceBrand, OsType, BrowserType 정보)
     */
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
