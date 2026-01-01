package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Outbox API 응답 DTO
 *
 * @param crawlTaskId Task ID
 * @param idempotencyKey 멱등성 키
 * @param status 상태
 * @param retryCount 재시도 횟수
 * @param createdAt 생성 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param updatedAt 수정 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param processedAt 처리 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Outbox 정보")
public record CrawlTaskOutboxApiResponse(
        @Schema(description = "Task ID", example = "1") Long crawlTaskId,
        @Schema(description = "멱등성 키", example = "outbox-1-abc12345") String idempotencyKey,
        @Schema(description = "상태", example = "PENDING") String status,
        @Schema(description = "재시도 횟수", example = "0") int retryCount,
        @Schema(description = "생성 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String createdAt,
        @Schema(description = "수정 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String updatedAt,
        @Schema(description = "처리 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String processedAt) {}
