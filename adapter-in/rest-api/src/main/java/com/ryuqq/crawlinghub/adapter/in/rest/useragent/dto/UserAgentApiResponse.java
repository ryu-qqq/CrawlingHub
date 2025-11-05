package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * UserAgent API Response DTO
 * <p>
 * REST API Layer의 불변 Response 객체입니다.
 * Java Record로 구현하여 불변성을 보장합니다.
 * </p>
 *
 * @param userAgentId UserAgent ID
 * @param userAgentString User-Agent 문자열
 * @param tokenStatus 토큰 상태
 * @param remainingRequests 남은 요청 수
 * @param tokenIssuedAt 토큰 발급 시간
 * @param rateLimitResetAt Rate Limit 리셋 시간
 * @param createdAt 생성 시간
 * @param updatedAt 수정 시간
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record UserAgentApiResponse(
        @JsonProperty("userAgentId")
        Long userAgentId,

        @JsonProperty("userAgentString")
        String userAgentString,

        @JsonProperty("tokenStatus")
        String tokenStatus,

        @JsonProperty("remainingRequests")
        Integer remainingRequests,

        @JsonProperty("tokenIssuedAt")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime tokenIssuedAt,

        @JsonProperty("rateLimitResetAt")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime rateLimitResetAt,

        @JsonProperty("createdAt")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonProperty("updatedAt")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
}

