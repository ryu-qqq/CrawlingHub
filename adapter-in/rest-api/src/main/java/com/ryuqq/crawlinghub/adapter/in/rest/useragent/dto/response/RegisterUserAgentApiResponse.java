package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * UserAgent 등록 API 응답 DTO
 *
 * <p>UserAgent 등록 성공 시 반환되는 응답입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "UserAgent 등록 응답")
public record RegisterUserAgentApiResponse(
        @Schema(description = "생성된 UserAgent ID", example = "12345") Long userAgentId,
        @Schema(description = "응답 메시지", example = "UserAgent registered successfully")
                String message) {

    /**
     * 등록 성공 응답 생성
     *
     * @param userAgentId 생성된 UserAgent ID
     * @return RegisterUserAgentApiResponse
     */
    public static RegisterUserAgentApiResponse of(Long userAgentId) {
        return new RegisterUserAgentApiResponse(userAgentId, "UserAgent registered successfully");
    }
}
