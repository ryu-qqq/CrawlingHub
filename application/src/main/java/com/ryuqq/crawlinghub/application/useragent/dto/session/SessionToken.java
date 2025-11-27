package com.ryuqq.crawlinghub.application.useragent.dto.session;

import java.time.Instant;

/**
 * 세션 토큰 정보
 *
 * <p>외부 사이트에서 발급받은 세션 토큰과 만료 시간을 담는 DTO
 *
 * @author development-team
 * @since 1.0.0
 */
public record SessionToken(String token, Instant expiresAt) {
    /**
     * 세션이 유효한지 확인
     *
     * @param now 현재 시간
     * @return 만료되지 않았으면 true
     */
    public boolean isValid(Instant now) {
        return expiresAt != null && now.isBefore(expiresAt);
    }

    /**
     * 세션이 만료되었는지 확인
     *
     * @param now 현재 시간
     * @return 만료되었으면 true
     */
    public boolean isExpired(Instant now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }
}
