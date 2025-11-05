package com.ryuqq.crawlinghub.domain.useragent.exception;

import java.util.Map;

/**
 * Rate Limit 초과 예외
 *
 * <p>User-Agent의 요청 한도를 초과했을 때 발생합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public final class RateLimitExceededException extends UserAgentException {

    private final Long userAgentId;
    private final Integer remainingRequests;

    /**
     * Rate Limit 초과 예외 생성
     *
     * @param userAgentId Rate Limit을 초과한 User-Agent ID
     * @param remainingRequests 남은 요청 수
     */
    public RateLimitExceededException(Long userAgentId, Integer remainingRequests) {
        super(String.format("User-Agent의 Rate Limit을 초과했습니다: %d (남은 요청: %d)", userAgentId, remainingRequests));
        this.userAgentId = userAgentId;
        this.remainingRequests = remainingRequests;
    }

    /**
     * Rate Limit을 초과한 User-Agent ID 반환
     *
     * @return User-Agent ID
     */
    public Long getUserAgentId() {
        return userAgentId;
    }

    /**
     * 남은 요청 수 반환
     *
     * @return 남은 요청 수
     */
    public Integer getRemainingRequests() {
        return remainingRequests;
    }

    @Override
    public String code() {
        return UserAgentErrorCode.RATE_LIMIT_EXCEEDED.getCode();
    }

    @Override
    public String message() {
        return getMessage();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of(
            "userAgentId", userAgentId,
            "remainingRequests", remainingRequests
        );
    }
}

