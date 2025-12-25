package com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Outbox API 응답 DTO
 *
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Outbox 정보")
public record OutboxApiResponse(
        @Schema(description = "Task ID", example = "1") Long crawlTaskId,
        @Schema(description = "멱등성 키", example = "outbox-1-abc12345") String idempotencyKey,
        @Schema(description = "상태", example = "PENDING") String status,
        @Schema(description = "재시도 횟수", example = "0") int retryCount,
        @Schema(description = "생성 시각", example = "2025-01-15T10:30:00Z") String createdAt,
        @Schema(description = "처리 시각", example = "2025-01-15T10:35:00Z") String processedAt) {}
