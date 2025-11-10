package com.ryuqq.crawlinghub.application.useragent.dto.response;

import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;

import java.time.LocalDateTime;

/**
 * UserAgent Response DTO
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
public record UserAgentResponse(
    Long userAgentId,
    String userAgentString,
    TokenStatus tokenStatus,
    Integer remainingRequests,
    LocalDateTime tokenIssuedAt,
    LocalDateTime rateLimitResetAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}



