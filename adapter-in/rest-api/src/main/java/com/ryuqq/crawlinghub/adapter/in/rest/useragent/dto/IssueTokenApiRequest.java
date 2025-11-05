package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 발급 API Request DTO
 * <p>
 * REST API Layer의 불변 Request 객체입니다.
 * Java Record로 구현하여 불변성을 보장합니다.
 * </p>
 *
 * @param token 발급할 토큰 (필수)
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record IssueTokenApiRequest(
        @JsonProperty("token")
        @NotBlank(message = "token은 필수입니다")
        String token
) {
    /**
     * Compact Constructor - 추가 검증 및 null 방어
     */
    public IssueTokenApiRequest {
        if (token != null) {
            token = token.trim();
        }
    }
}

