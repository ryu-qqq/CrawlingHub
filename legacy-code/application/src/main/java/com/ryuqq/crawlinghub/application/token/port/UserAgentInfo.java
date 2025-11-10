package com.ryuqq.crawlinghub.application.legacy.token.port;

import java.time.LocalDateTime;

/**
 * User-Agent 정보
 * DB에서 조회한 User-Agent와 Token 정보
 *
 * @author crawlinghub
 */
public record UserAgentInfo(
        Long agentId,
        String userAgent,
        Long tokenId,
        String tokenValue,
        LocalDateTime expiresAt
) {
    /**
     * Token이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Token이 유효한지 확인
     */
    public boolean isValid() {
        return tokenId != null && tokenValue != null && !isExpired();
    }
}
