package com.ryuqq.crawlinghub.domain.useragent.vo;

/**
 * UserAgent 상태 Enum (HikariCP 벤치마킹)
 *
 * <p><strong>상태 흐름</strong>:
 *
 * <pre>
 *           register()
 *               │
 *               ▼
 *       SESSION_REQUIRED ◄─── expireSession() ───┐
 *               │                                 │
 *         issueSession()                          │
 *               │                                 │
 *               ▼                                 │
 *      ┌──── IDLE ◄──── return(success) ─────────┤
 *      │       │                                  │
 *  borrow()    │                                  │
 *      │       ▼                                  │
 *      │   BORROWED ── return(success) ──► IDLE   │
 *      │       │                                  │
 *      │       │ return(429)                      │
 *      │       ▼                                  │
 *      │   COOLDOWN ── cooldown만료 ──► IDLE ─────┘
 *      │       │         (or SESSION_REQUIRED)
 *      │       │ (연속429, health&lt;threshold)
 *      │       ▼
 *      └── SUSPENDED ── admin recover ──► SESSION_REQUIRED
 *
 * BLOCKED (영구 차단 - Pool에서 제외)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum UserAgentStatus {

    /** 풀에서 대기 중, 즉시 사용 가능 (HikariCP NOT_IN_USE 대응) */
    IDLE("대기 중"),

    /** 크롤링에 사용 중 - Redis only (HikariCP IN_USE 대응) */
    BORROWED("사용 중"),

    /** 429 후 자동 회복 대기 (Graduated Backoff) */
    COOLDOWN("쿨다운 대기"),

    /**
     * 세션 발급이 필요한 상태
     *
     * <ul>
     *   <li>Pool에 새로 추가됨
     *   <li>세션이 만료됨
     *   <li>COOLDOWN에서 복구 시 세션 만료
     * </ul>
     */
    SESSION_REQUIRED("세션 발급 필요"),

    /**
     * 관리자 개입 필요 (HikariCP REMOVED 대응)
     *
     * <ul>
     *   <li>연속 429 5회 이상
     *   <li>Health Score &lt; 30
     * </ul>
     */
    SUSPENDED("일시 정지"),

    /** 영구 차단 - 관리자 차단, Pool에서 완전 제외 */
    BLOCKED("영구 차단");

    private final String description;

    UserAgentStatus(String description) {
        this.description = description;
    }

    /**
     * 상태 설명 반환
     *
     * @return 상태 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 풀에서 대기 중인지 확인
     *
     * @return IDLE이면 true
     */
    public boolean isIdle() {
        return this == IDLE;
    }

    /**
     * 사용 중인지 확인
     *
     * @return BORROWED이면 true
     */
    public boolean isBorrowed() {
        return this == BORROWED;
    }

    /**
     * 쿨다운 대기 중인지 확인
     *
     * @return COOLDOWN이면 true
     */
    public boolean isCooldown() {
        return this == COOLDOWN;
    }

    /**
     * 즉시 사용 가능한지 확인 (기존 isReady 대체)
     *
     * @return IDLE이면 true
     */
    public boolean isReady() {
        return this == IDLE;
    }

    /**
     * 세션 발급이 필요한지 확인
     *
     * @return SESSION_REQUIRED이면 true
     */
    public boolean needsSession() {
        return this == SESSION_REQUIRED;
    }

    /**
     * 정지 상태인지 확인
     *
     * @return SUSPENDED이면 true
     */
    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    /**
     * borrow 가능한 상태인지 확인
     *
     * @return IDLE이면 true
     */
    public boolean canBorrow() {
        return this == IDLE;
    }

    /**
     * 복구 가능 상태인지 확인 (관리자 개입)
     *
     * @return SUSPENDED이면 true (BLOCKED는 복구 불가)
     */
    public boolean canRecover() {
        return this == SUSPENDED;
    }

    /**
     * 쿨다운에서 자동 복구 가능한 상태인지 확인
     *
     * @return COOLDOWN이면 true
     */
    public boolean canAutoRecover() {
        return this == COOLDOWN;
    }

    /**
     * 영구 차단 상태인지 확인
     *
     * @return BLOCKED면 true
     */
    public boolean isBlocked() {
        return this == BLOCKED;
    }

    /**
     * 활성 Pool에 포함된 상태인지 확인
     *
     * <p><strong>활성 Pool 상태</strong>:
     *
     * <ul>
     *   <li>IDLE: 즉시 사용 가능
     *   <li>SESSION_REQUIRED: 세션 발급 대기 중
     * </ul>
     *
     * @return IDLE 또는 SESSION_REQUIRED이면 true
     */
    public boolean isAvailableInPool() {
        return this == IDLE || this == SESSION_REQUIRED;
    }

    /**
     * 활성 Pool에 포함된 상태인지 확인 (기존 호환용)
     *
     * @return IDLE 또는 SESSION_REQUIRED이면 true
     */
    public boolean isAvailable() {
        return isAvailableInPool();
    }
}
