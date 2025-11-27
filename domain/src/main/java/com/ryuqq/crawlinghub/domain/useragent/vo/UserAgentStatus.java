package com.ryuqq.crawlinghub.domain.useragent.vo;

/**
 * UserAgent 상태 Enum
 *
 * <p><strong>상태 흐름</strong>:
 *
 * <pre>
 * AVAILABLE ←→ SUSPENDED (자동 복구)
 *     ↓
 *  BLOCKED (영구 차단)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum UserAgentStatus {

    /** 사용 가능 - 정상 상태 */
    AVAILABLE("사용 가능"),

    /** 일시 정지 - Health Score 낮음 또는 429 응답 */
    SUSPENDED("일시 정지"),

    /** 영구 차단 - 관리자 차단 */
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
     * 사용 가능 상태인지 확인
     *
     * @return AVAILABLE이면 true
     */
    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    /**
     * 복구 가능 상태인지 확인
     *
     * @return SUSPENDED면 true (BLOCKED는 복구 불가)
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
}
