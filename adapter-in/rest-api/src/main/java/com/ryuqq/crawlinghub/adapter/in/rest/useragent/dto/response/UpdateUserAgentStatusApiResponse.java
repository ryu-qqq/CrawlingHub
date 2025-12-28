package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * UserAgent 상태 일괄 변경 API Response
 *
 * <p>UserAgent 상태 일괄 변경 결과를 반환합니다.
 *
 * @param updatedCount 상태가 변경된 UserAgent 수
 * @param message 결과 메시지
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "UserAgent 상태 일괄 변경 응답")
public record UpdateUserAgentStatusApiResponse(
        @Schema(description = "상태가 변경된 UserAgent 수", example = "5") int updatedCount,
        @Schema(description = "결과 메시지", example = "5 user agents status updated to SUSPENDED")
                String message) {

    /**
     * 상태 변경 결과 응답 생성
     *
     * @param updatedCount 변경된 UserAgent 수
     * @param status 변경된 상태
     * @return UpdateUserAgentStatusApiResponse
     */
    public static UpdateUserAgentStatusApiResponse of(int updatedCount, String status) {
        String message =
                String.format("%d user agent(s) status updated to %s", updatedCount, status);
        return new UpdateUserAgentStatusApiResponse(updatedCount, message);
    }
}
