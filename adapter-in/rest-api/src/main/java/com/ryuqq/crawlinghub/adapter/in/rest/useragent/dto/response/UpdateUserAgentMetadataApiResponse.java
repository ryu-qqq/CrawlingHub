package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * UserAgent 메타데이터 수정 API 응답 DTO
 *
 * <p>UserAgent 메타데이터 수정 성공 시 반환되는 응답입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "UserAgent 메타데이터 수정 응답")
public record UpdateUserAgentMetadataApiResponse(
        @Schema(description = "수정된 UserAgent ID", example = "12345") Long userAgentId,
        @Schema(description = "응답 메시지", example = "UserAgent metadata updated successfully")
                String message) {

    /**
     * 수정 성공 응답 생성
     *
     * @param userAgentId 수정된 UserAgent ID
     * @return UpdateUserAgentMetadataApiResponse
     */
    public static UpdateUserAgentMetadataApiResponse of(Long userAgentId) {
        return new UpdateUserAgentMetadataApiResponse(
                userAgentId, "UserAgent metadata updated successfully");
    }
}
