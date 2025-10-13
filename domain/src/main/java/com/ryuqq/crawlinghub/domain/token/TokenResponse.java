package com.ryuqq.crawlinghub.domain.token;

import java.time.Instant;

/**
 * 토큰 발급/갱신 응답 도메인 모델
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        Instant issuedAt
) {
    /**
     * 토큰 응답 생성
     */
    public static TokenResponse of(
            String accessToken,
            String refreshToken,
            long expiresIn,
            String tokenType
    ) {
        return new TokenResponse(
                accessToken,
                refreshToken,
                expiresIn,
                tokenType,
                Instant.now()
        );
    }

    /**
     * 토큰 만료 시간 계산
     */
    public Instant getExpirationTime() {
        return issuedAt.plusSeconds(expiresIn);
    }

    /**
     * 토큰 만료 여부
     */
    public boolean isExpired() {
        return Instant.now().isAfter(getExpirationTime());
    }

    /**
     * 토큰 갱신이 필요한지 여부 (만료 5분 전)
     */
    public boolean needsRefresh() {
        Instant refreshThreshold = getExpirationTime().minusSeconds(300);
        return Instant.now().isAfter(refreshThreshold);
    }
}
