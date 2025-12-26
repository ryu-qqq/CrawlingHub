package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;

/**
 * UserAgent Bounded Context 에러 코드 Enum
 *
 * @author development-team
 * @since 1.0.0
 */
public enum UserAgentErrorCode implements ErrorCode {

    /** UserAgent를 찾을 수 없음 */
    USER_AGENT_NOT_FOUND("USER-AGENT-001", 404, "존재하지 않는 UserAgent입니다."),

    /** 유효하지 않은 상태 전환 */
    INVALID_USER_AGENT_STATE("USER-AGENT-002", 400, "유효하지 않은 상태 전환입니다."),

    /** 사용 가능한 UserAgent 없음 */
    NO_AVAILABLE_USER_AGENT("USER-AGENT-003", 503, "사용 가능한 UserAgent가 없습니다."),

    /** Rate Limit 초과 */
    RATE_LIMIT_EXCEEDED("USER-AGENT-004", 429, "Rate Limit을 초과했습니다."),

    /** Circuit Breaker Open */
    CIRCUIT_BREAKER_OPEN("USER-AGENT-005", 503, "UserAgent Pool Circuit Breaker가 열렸습니다."),

    /** UserAgent 차단됨 */
    USER_AGENT_BLOCKED("USER-AGENT-006", 403, "차단된 UserAgent입니다."),

    /** 유효하지 않은 토큰 */
    INVALID_TOKEN("USER-AGENT-007", 400, "유효하지 않은 토큰 형식입니다."),

    /** 세션 발급 실패 */
    SESSION_ISSUANCE_FAILED("USER-AGENT-008", 503, "세션 토큰 발급에 실패했습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;

    UserAgentErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
