package com.ryuqq.crawlinghub.application.useragent.dto.cache;

import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;

/**
 * Redis Pool 저장용 UserAgent DTO
 *
 * <p><strong>Redis에 저장되는 UserAgent 정보</strong>:
 *
 * <ul>
 *   <li>userAgentValue: User-Agent 헤더 값
 *   <li>sessionToken: 발급받은 세션 토큰
 *   <li>nid: nid 쿠키 값 (Search API용)
 *   <li>mustitUid: mustit_uid 쿠키 값 (Search API용)
 *   <li>sessionExpiresAt: 세션 만료 시간
 *   <li>remainingTokens: 남은 요청 토큰 수 (초기 80)
 *   <li>healthScore: 건강 점수 (0-100)
 *   <li>status: IDLE, BORROWED, COOLDOWN, SESSION_REQUIRED, SUSPENDED
 *   <li>borrowedAt: BORROWED 시작 시각 (Leak Detection용)
 *   <li>cooldownUntil: COOLDOWN 만료 시각
 *   <li>consecutiveRateLimits: 연속 429 횟수
 * </ul>
 *
 * <p><strong>세션 흐름</strong>:
 *
 * <pre>
 * Pool 추가 (SESSION_REQUIRED)
 *     ↓ 세션 발급
 * IDLE (사용 가능)
 *     ↓ borrow()
 * BORROWED (사용 중)
 *     ↓ return(success) → IDLE
 *     ↓ return(429) → COOLDOWN → IDLE
 *     ↓ return(429 연속) → SUSPENDED
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public record CachedUserAgent(
        Long userAgentId,
        String userAgentValue,
        String sessionToken,
        String nid,
        String mustitUid,
        Instant sessionExpiresAt,
        int remainingTokens,
        int maxTokens,
        Instant windowStart,
        Instant windowEnd,
        int healthScore,
        UserAgentStatus status,
        Instant suspendedAt,
        Instant borrowedAt,
        Instant cooldownUntil,
        int consecutiveRateLimits) {
    private static final int DEFAULT_MAX_TOKENS = 80;

    /**
     * 신규 UserAgent를 Redis Pool에 추가하기 위한 DTO 생성
     *
     * <p>세션이 없으므로 SESSION_REQUIRED 상태로 시작
     *
     * @param userAgent Domain UserAgent
     * @return CachedUserAgent (SESSION_REQUIRED, 세션 없음)
     */
    public static CachedUserAgent forNew(UserAgent userAgent) {
        return new CachedUserAgent(
                userAgent.getIdValue(),
                userAgent.getUserAgentStringValue(),
                null,
                null,
                null,
                null,
                DEFAULT_MAX_TOKENS,
                DEFAULT_MAX_TOKENS,
                null,
                null,
                userAgent.getHealthScoreValue(),
                UserAgentStatus.SESSION_REQUIRED,
                null,
                null,
                null,
                0);
    }

    /**
     * DB 폴백용 CachedUserAgent 생성
     *
     * <p>Redis 장애 시 DB UserAgent로부터 생성합니다. 세션 정보가 없으므로 degraded mode로 동작합니다.
     *
     * @param userAgent Domain UserAgent (available 상태)
     * @return CachedUserAgent (IDLE, 세션 없음, degraded mode)
     */
    public static CachedUserAgent forDbFallback(UserAgent userAgent) {
        return new CachedUserAgent(
                userAgent.getIdValue(),
                userAgent.getUserAgentStringValue(),
                null,
                null,
                null,
                null,
                DEFAULT_MAX_TOKENS,
                DEFAULT_MAX_TOKENS,
                null,
                null,
                userAgent.getHealthScoreValue(),
                UserAgentStatus.IDLE,
                null,
                null,
                null,
                0);
    }

    /**
     * 복구된 UserAgent를 위한 DTO 생성
     *
     * <p>복구 시에도 세션 재발급이 필요하므로 SESSION_REQUIRED
     *
     * @param userAgentId UserAgent ID
     * @param userAgentValue User-Agent 헤더 값
     * @return CachedUserAgent (SESSION_REQUIRED, Health=70)
     */
    public static CachedUserAgent forRecovery(Long userAgentId, String userAgentValue) {
        return new CachedUserAgent(
                userAgentId,
                userAgentValue,
                null,
                null,
                null,
                null,
                DEFAULT_MAX_TOKENS,
                DEFAULT_MAX_TOKENS,
                null,
                null,
                70,
                UserAgentStatus.SESSION_REQUIRED,
                null,
                null,
                null,
                0);
    }

    /**
     * 세션 발급 후 IDLE 상태로 전환
     *
     * @param sessionToken 발급받은 세션 토큰
     * @param sessionExpiresAt 세션 만료 시간
     * @return 새로운 CachedUserAgent (IDLE 상태)
     */
    public CachedUserAgent withSession(String sessionToken, Instant sessionExpiresAt) {
        return new CachedUserAgent(
                this.userAgentId,
                this.userAgentValue,
                sessionToken,
                this.nid,
                this.mustitUid,
                sessionExpiresAt,
                this.remainingTokens,
                this.maxTokens,
                this.windowStart,
                this.windowEnd,
                this.healthScore,
                UserAgentStatus.IDLE,
                null,
                null,
                null,
                0);
    }

    /**
     * 세션 및 쿠키 발급 후 IDLE 상태로 전환
     *
     * @param sessionToken 발급받은 세션 토큰
     * @param nid nid 쿠키 값
     * @param mustitUid mustit_uid 쿠키 값
     * @param sessionExpiresAt 세션 만료 시간
     * @return 새로운 CachedUserAgent (IDLE 상태)
     */
    public CachedUserAgent withSession(
            String sessionToken, String nid, String mustitUid, Instant sessionExpiresAt) {
        return new CachedUserAgent(
                this.userAgentId,
                this.userAgentValue,
                sessionToken,
                nid,
                mustitUid,
                sessionExpiresAt,
                this.remainingTokens,
                this.maxTokens,
                this.windowStart,
                this.windowEnd,
                this.healthScore,
                UserAgentStatus.IDLE,
                null,
                null,
                null,
                0);
    }

    /**
     * 토큰이 남아있는지 확인
     *
     * @return remainingTokens > 0이면 true
     */
    public boolean hasTokens() {
        return remainingTokens > 0;
    }

    /**
     * 세션이 유효한지 확인
     *
     * @param now 현재 시간
     * @return 세션이 있고 만료되지 않았으면 true
     */
    public boolean hasValidSession(Instant now) {
        return sessionToken != null && sessionExpiresAt != null && now.isBefore(sessionExpiresAt);
    }

    /**
     * 세션이 만료되었는지 확인
     *
     * @param now 현재 시간
     * @return 세션이 만료되었으면 true
     */
    public boolean isSessionExpired(Instant now) {
        return sessionExpiresAt != null && now.isAfter(sessionExpiresAt);
    }

    /**
     * Sliding Window가 만료되었는지 확인
     *
     * @param now 현재 시간
     * @return windowEnd가 현재 시간보다 이전이면 true
     */
    public boolean isWindowExpired(Instant now) {
        return windowEnd != null && now.isAfter(windowEnd);
    }

    /**
     * 즉시 사용 가능한 상태인지 확인 (IDLE 상태)
     *
     * @return IDLE이면 true
     */
    public boolean isReady() {
        return status.isIdle();
    }

    /**
     * 풀에서 대기 중인지 확인
     *
     * @return IDLE이면 true
     */
    public boolean isIdle() {
        return status.isIdle();
    }

    /**
     * 사용 중인지 확인
     *
     * @return BORROWED이면 true
     */
    public boolean isBorrowed() {
        return status.isBorrowed();
    }

    /**
     * 쿨다운 대기 중인지 확인
     *
     * @return COOLDOWN이면 true
     */
    public boolean isCooldown() {
        return status.isCooldown();
    }

    /**
     * 세션 발급이 필요한 상태인지 확인
     *
     * @return SESSION_REQUIRED이면 true
     */
    public boolean needsSession() {
        return status.needsSession();
    }

    /**
     * 복구 가능한 상태인지 확인 (SUSPENDED + 지정 시간 경과 + Health >= 30)
     *
     * @param threshold 복구 기준 시점 (이 시간 이전에 suspended되었어야 함)
     * @return 복구 가능하면 true
     */
    public boolean isRecoverable(Instant threshold) {
        return status.isSuspended()
                && suspendedAt != null
                && suspendedAt.isBefore(threshold)
                && healthScore >= 30;
    }

    /**
     * Search API용 쿠키가 있는지 확인
     *
     * @return nid와 mustitUid가 모두 있으면 true
     */
    public boolean hasSearchCookies() {
        return nid != null && !nid.isBlank() && mustitUid != null && !mustitUid.isBlank();
    }
}
