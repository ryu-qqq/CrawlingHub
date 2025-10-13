package com.ryuqq.crawlinghub.domain.token;

/**
 * 획득된 User-Agent 토큰 정보
 *
 * @author crawlinghub
 */
public record AcquiredToken(
        Long userAgentId,
        String userAgent,
        String tokenValue,
        String lockKey,
        String lockValue
) {
    /**
     * 토큰 획득 성공 여부
     */
    public boolean isAcquired() {
        return userAgentId != null && tokenValue != null;
    }

    /**
     * 락 획득 성공 여부
     */
    public boolean hasLock() {
        return lockKey != null && lockValue != null;
    }
}
