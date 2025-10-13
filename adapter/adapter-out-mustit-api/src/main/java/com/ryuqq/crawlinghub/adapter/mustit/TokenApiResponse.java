package com.ryuqq.crawlinghub.adapter.mustit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ryuqq.crawlinghub.domain.token.TokenResponse;

/**
 * 머스트잇 API 토큰 응답 DTO
 *
 * @param accessToken 액세스 토큰
 * @param refreshToken 리프레시 토큰
 * @param expiresIn 만료 시간 (초)
 * @param tokenType 토큰 타입
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
public record TokenApiResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("expires_in")
        long expiresIn,

        @JsonProperty("token_type")
        String tokenType
) {
    /**
     * 도메인 모델로 변환
     */
    public TokenResponse toDomain() {
        return TokenResponse.of(
                accessToken,
                refreshToken,
                expiresIn,
                tokenType
        );
    }
}
