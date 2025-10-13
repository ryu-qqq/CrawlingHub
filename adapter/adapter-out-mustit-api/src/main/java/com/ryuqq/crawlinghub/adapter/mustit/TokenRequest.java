package com.ryuqq.crawlinghub.adapter.mustit;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 토큰 발급 요청 DTO
 *
 * @param userAgent User-Agent 헤더
 * @param grantType 권한 부여 타입
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
public record TokenRequest(
        @JsonProperty("user_agent")
        String userAgent,

        @JsonProperty("grant_type")
        String grantType
) {
    /**
     * 신규 토큰 발급 요청 생성
     */
    public static TokenRequest forIssue(String userAgent) {
        return new TokenRequest(userAgent, "client_credentials");
    }

    /**
     * 토큰 갱신 요청 생성
     */
    public static TokenRequest forRefresh(String refreshToken) {
        return new TokenRequest(refreshToken, "refresh_token");
    }
}
