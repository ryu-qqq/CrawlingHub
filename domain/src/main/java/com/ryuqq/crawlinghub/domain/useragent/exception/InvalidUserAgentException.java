package com.ryuqq.crawlinghub.domain.useragent.exception;

import java.util.Map;

/**
 * 유효하지 않은 User-Agent 문자열 예외
 *
 * <p>User-Agent 문자열이 null이거나 빈 문자열일 때 발생합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public final class InvalidUserAgentException extends UserAgentException {

    private final String userAgentString;

    /**
     * 유효하지 않은 User-Agent 예외 생성
     *
     * @param userAgentString 유효하지 않은 User-Agent 문자열
     */
    public InvalidUserAgentException(String userAgentString) {
        super(String.format("유효하지 않은 User-Agent 문자열입니다: %s", userAgentString));
        this.userAgentString = userAgentString;
    }

    /**
     * 유효하지 않았던 User-Agent 문자열 반환
     *
     * @return User-Agent 문자열
     */
    public String getUserAgentString() {
        return userAgentString;
    }

    @Override
    public String code() {
        return UserAgentErrorCode.INVALID_USER_AGENT.getCode();
    }

    @Override
    public String message() {
        return getMessage();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of("userAgentString", userAgentString != null ? userAgentString : "null");
    }
}



