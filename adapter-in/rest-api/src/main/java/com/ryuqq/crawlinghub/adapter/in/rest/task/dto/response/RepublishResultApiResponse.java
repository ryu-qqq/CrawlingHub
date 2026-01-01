package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Outbox 재발행 결과 API 응답 DTO
 *
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Outbox 재발행 결과")
public record RepublishResultApiResponse(
        @Schema(description = "Task ID", example = "1") Long crawlTaskId,
        @Schema(description = "성공 여부", example = "true") boolean success,
        @Schema(description = "결과 메시지", example = "SQS 재발행이 완료되었습니다.") String message) {}
