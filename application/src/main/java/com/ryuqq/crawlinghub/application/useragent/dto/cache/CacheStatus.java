package com.ryuqq.crawlinghub.application.useragent.dto.cache;

/**
 * Redis Cache 전용 UserAgent 상태
 *
 * <p>Domain의 UserAgentStatus와 분리하여 캐시 레이어의 운영 상태를 관리합니다.
 *
 * <p><strong>상태 흐름</strong>:
 *
 * <pre>
 * SESSION_REQUIRED (초기 또는 세션 만료)
 *       ↓ 세션 발급 성공
 *     READY (사용 가능)
 *       ↓ 429 응답 또는 Health < 30
 *   SUSPENDED (일시 정지)
 *       ↓ 1시간 경과 + Health ≥ 30
 * SESSION_REQUIRED (복구 대기)
 * </pre>
 *
 * <p><strong>Domain UserAgentStatus와의 관계</strong>:
 *
 * <ul>
 *   <li>Domain AVAILABLE → Cache READY 또는 SESSION_REQUIRED
 *   <li>Domain SUSPENDED → Cache SUSPENDED
 *   <li>Domain BLOCKED → Cache에 존재하지 않음
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CacheStatus {

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
     *   <li>Health Score < 30
     * </ul>
     */
    SUSPENDED("일시 정지");

    private final String description;

    CacheStatus(String description) {
        this.description = description;
    }

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
     * 복구 가능한 상태인지 확인
     *
     * @return SUSPENDED이면 true
     */
    public boolean canRecover() {
        return this == SUSPENDED;
    }
}
