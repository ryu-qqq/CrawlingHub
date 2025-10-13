package com.ryuqq.crawlinghub.adapter.mustit;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 토큰 갱신 요청 DTO
 *
 * @param refreshToken 갱신 토큰
 * @param grantType 권한 부여 타입 (refresh_token)
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
public record RefreshTokenRequest(
        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("grant_type")
        String grantType
) {
    /**
     * 토큰 갱신 요청 생성
     */
    public static RefreshTokenRequest of(String refreshToken) {
        return new RefreshTokenRequest(refreshToken, "refresh_token");
    }
}
