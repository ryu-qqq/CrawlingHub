package com.ryuqq.crawlinghub.domain.useragent.exception;

import java.util.Map;

/**
 * 토큰 만료 예외
 *
 * <p>User-Agent의 토큰이 만료되었을 때 발생합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public final class TokenExpiredException extends UserAgentException {

    private final Long userAgentId;

    /**
     * 토큰 만료 예외 생성
     *
     * @param userAgentId 토큰이 만료된 User-Agent ID
     */
    public TokenExpiredException(Long userAgentId) {
        super(String.format("User-Agent의 토큰이 만료되었습니다: %d", userAgentId));
        this.userAgentId = userAgentId;
    }

    /**
     * 토큰이 만료된 User-Agent ID 반환
     *
     * @return User-Agent ID
     */
    public Long getUserAgentId() {
        return userAgentId;
    }

    @Override
    public String code() {
        return UserAgentErrorCode.TOKEN_EXPIRED.getCode();
    }

    @Override
    public String message() {
        return getMessage();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of("userAgentId", userAgentId);
    }
}

