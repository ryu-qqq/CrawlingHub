package com.ryuqq.crawlinghub.domain.useragent.vo;

/**
 * UserAgent 상태 Enum
 *
 * <p><strong>상태 흐름</strong>:
 *
 * <pre>
 * SESSION_REQUIRED (Pool 추가, 복구)
 *       ↓ 세션 발급 성공
 *     READY (세션 있음, 사용 가능)
 *       ↓ 429 응답 또는 Health &lt; 30
 *   SUSPENDED (일시 정지)
 *       ↓ 1시간 경과 + Health ≥ 30
 * SESSION_REQUIRED (복구 대기)
 *
 * BLOCKED (영구 차단 - Pool에서 제외)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum UserAgentStatus {

    /** 세션이 있고 즉시 사용 가능한 상태 */
    READY("사용 가능"),

    /**
     * 세션 발급이 필요한 상태
     *
     * <ul>
     *   <li>Pool에 새로 추가됨
     *   <li>세션이 만료됨
     *   <li>SUSPENDED에서 복구됨
     * </ul>
     */
    SESSION_REQUIRED("세션 발급 필요"),

    /**
     * 일시 정지 상태
     *
     * <ul>
     *   <li>429 응답 수신
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
     * 즉시 사용 가능한지 확인
     *
     * @return READY이면 true
     */
    public boolean isReady() {
        return this == READY;
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
     * 복구 가능 상태인지 확인
     *
     * @return SUSPENDED이면 true (BLOCKED는 복구 불가)
     */
    public boolean canRecover() {
        return this == SUSPENDED;
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
     * 사용 가능 상태인지 확인 (READY 또는 SESSION_REQUIRED)
     *
     * @return READY 또는 SESSION_REQUIRED이면 true
     */
    public boolean isAvailable() {
        return this == READY || this == SESSION_REQUIRED;
    }
}
