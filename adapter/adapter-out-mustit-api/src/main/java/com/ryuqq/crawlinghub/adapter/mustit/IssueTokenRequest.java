package com.ryuqq.crawlinghub.adapter.mustit;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 토큰 발급 요청 DTO
 *
 * @param userAgent User-Agent 헤더
 * @param grantType 권한 부여 타입 (client_credentials)
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
public record IssueTokenRequest(
        @JsonProperty("user_agent")
        String userAgent,

        @JsonProperty("grant_type")
        String grantType
) {
    /**
     * 신규 토큰 발급 요청 생성
     */
    public static IssueTokenRequest of(String userAgent) {
        return new IssueTokenRequest(userAgent, "client_credentials");
    }
}
