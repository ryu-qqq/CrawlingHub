package com.ryuqq.crawlinghub.domain.useragent.exception;

import java.util.Map;

/**
 * 사용 가능한 User-Agent 없음 예외
 *
 * <p>사용 가능한 User-Agent를 찾을 수 없을 때 발생합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public final class NoAvailableUserAgentException extends UserAgentException {

    /**
     * 사용 가능한 User-Agent 없음 예외 생성
     */
    public NoAvailableUserAgentException() {
        super("사용 가능한 User-Agent를 찾을 수 없습니다");
    }

    @Override
    public String code() {
        return UserAgentErrorCode.NO_AVAILABLE_USER_AGENT.getCode();
    }

    @Override
    public String message() {
        return getMessage();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of();
    }
}

