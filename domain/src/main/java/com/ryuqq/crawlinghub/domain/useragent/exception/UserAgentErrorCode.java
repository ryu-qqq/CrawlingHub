package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.ErrorCode;

/**
 * UserAgent Bounded Context 전용 ErrorCode
 *
 * <p>UserAgent 도메인에서 발생하는 모든 비즈니스 예외의 에러 코드를 정의합니다.</p>
 *
 * <p><strong>코드 체계:</strong></p>
 * <ul>
 *   <li>USER_AGENT-001 ~ USER_AGENT-009: Not Found (404)</li>
 *   <li>USER_AGENT-010 ~ USER_AGENT-099: Conflict (409)</li>
 *   <li>USER_AGENT-101 ~ USER_AGENT-199: Bad Request (400)</li>
 *   <li>USER_AGENT-201 ~ USER_AGENT-299: Too Many Requests (429)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public enum UserAgentErrorCode implements ErrorCode {

    /**
     * 사용 가능한 User-Agent를 찾을 수 없음
     */
    NO_AVAILABLE_USER_AGENT("USER_AGENT-001", 404, "사용 가능한 User-Agent를 찾을 수 없습니다", "No Available User Agent"),

    /**
     * 유효하지 않은 User-Agent 문자열
     */
    INVALID_USER_AGENT("USER_AGENT-101", 400, "유효하지 않은 User-Agent 문자열입니다", "Invalid User Agent"),

    /**
     * 토큰이 만료됨
     */
    TOKEN_EXPIRED("USER_AGENT-102", 400, "토큰이 만료되었습니다", "Token Expired"),

    /**
     * Rate Limit 초과
     */
    RATE_LIMIT_EXCEEDED("USER_AGENT-201", 429, "Rate Limit을 초과했습니다", "Rate Limit Exceeded");

    private final String code;
    private final int httpStatus;
    private final String message;
    private final String title;

    UserAgentErrorCode(String code, int httpStatus, String message, String title) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
        this.title = title;
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

    /**
     * HTTP 응답용 title 반환
     *
     * @return title (예: "No Available User Agent", "Invalid User Agent")
     */
    public String getTitle() {
        return title;
    }
}

