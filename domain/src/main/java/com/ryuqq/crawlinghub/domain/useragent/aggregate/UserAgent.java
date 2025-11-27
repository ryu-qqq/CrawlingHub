package com.ryuqq.crawlinghub.domain.useragent.aggregate;

import com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentStateException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;
import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.LocalDateTime;

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
    private final Token token;
    private UserAgentStatus status;
    private HealthScore healthScore;
    private LocalDateTime lastUsedAt;
    private int requestsPerDay;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserAgent(
            UserAgentId id,
            Token token,
            UserAgentStatus status,
            HealthScore healthScore,
            LocalDateTime lastUsedAt,
            int requestsPerDay,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.token = token;
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
     * @param token 암호화된 토큰
     * @return 새로운 UserAgent (AVAILABLE, Health Score 100)
     */
    public static UserAgent create(Token token) {
        LocalDateTime now = LocalDateTime.now();
        return new UserAgent(
                UserAgentId.unassigned(),
                token,
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
            UserAgentStatus status,
            HealthScore healthScore,
            LocalDateTime lastUsedAt,
            int requestsPerDay,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new UserAgent(
                id, token, status, healthScore, lastUsedAt, requestsPerDay, createdAt, updatedAt);
    }

    // === 비즈니스 로직 ===

    /** 사용 기록 (lastUsedAt, requestsPerDay 업데이트) */
    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.requestsPerDay++;
        this.updatedAt = LocalDateTime.now();
    }

    /** 성공 기록 (Health Score +5, 최대 100) */
    public void recordSuccess() {
        this.healthScore = this.healthScore.recordSuccess();
        this.lastUsedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
     */
    public void recordFailure(int httpStatusCode) {
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

        this.lastUsedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
     * @throws InvalidUserAgentStateException 현재 상태가 AVAILABLE이 아닌 경우
     */
    public void suspend() {
        if (!this.status.isAvailable()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.SUSPENDED);
        }
        this.status = UserAgentStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 복구 (SUSPENDED → AVAILABLE, Health Score 70)
     *
     * @throws InvalidUserAgentStateException 복구 불가능한 상태인 경우
     */
    public void recover() {
        if (!this.status.canRecover()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.AVAILABLE);
        }
        this.status = UserAgentStatus.AVAILABLE;
        this.healthScore = HealthScore.recovered();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 영구 차단 (AVAILABLE/SUSPENDED → BLOCKED)
     *
     * @throws InvalidUserAgentStateException 이미 BLOCKED 상태인 경우
     */
    public void block() {
        if (this.status.isBlocked()) {
            throw new InvalidUserAgentStateException(this.status, UserAgentStatus.BLOCKED);
        }
        this.status = UserAgentStatus.BLOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    /** 일일 요청 수 초기화 (매일 자정 실행) */
    public void resetDailyRequests() {
        this.requestsPerDay = 0;
        this.updatedAt = LocalDateTime.now();
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
    public boolean isRecoverable(LocalDateTime threshold) {
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

    public UserAgentStatus getStatus() {
        return status;
    }

    public HealthScore getHealthScore() {
        return healthScore;
    }

    public int getHealthScoreValue() {
        return healthScore.value();
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public int getRequestsPerDay() {
        return requestsPerDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
