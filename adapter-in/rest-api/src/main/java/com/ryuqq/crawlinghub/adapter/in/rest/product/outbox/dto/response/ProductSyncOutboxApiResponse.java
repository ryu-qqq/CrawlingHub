package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ProductSyncOutbox API 응답
 *
 * @param id Outbox ID
 * @param crawledProductId CrawledProduct ID
 * @param sellerId Seller ID
 * @param itemNo 상품 번호
 * @param syncType 동기화 타입 (CREATE, UPDATE)
 * @param idempotencyKey 멱등성 키
 * @param externalProductId 외부 상품 ID (nullable)
 * @param status 상태 (PENDING, PROCESSING, COMPLETED, FAILED)
 * @param retryCount 재시도 횟수
 * @param errorMessage 에러 메시지 (nullable)
 * @param canRetry 재시도 가능 여부
 * @param createdAt 생성 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param updatedAt 수정 시각 (yyyy-MM-dd HH:mm:ss 형식)
 * @param processedAt 처리 시각 (yyyy-MM-dd HH:mm:ss 형식, nullable)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "상품 동기화 Outbox 응답")
public record ProductSyncOutboxApiResponse(
        @Schema(description = "Outbox ID", example = "1") Long id,
        @Schema(description = "CrawledProduct ID", example = "100") Long crawledProductId,
        @Schema(description = "Seller ID", example = "10") Long sellerId,
        @Schema(description = "상품 번호", example = "12345") long itemNo,
        @Schema(description = "동기화 타입", example = "CREATE") String syncType,
        @Schema(description = "멱등성 키", example = "sync-10-12345-1234567890") String idempotencyKey,
        @Schema(description = "외부 상품 ID", example = "EXT-123") Long externalProductId,
        @Schema(description = "상태", example = "PENDING") String status,
        @Schema(description = "재시도 횟수", example = "0") int retryCount,
        @Schema(description = "에러 메시지") String errorMessage,
        @Schema(description = "재시도 가능 여부", example = "true") boolean canRetry,
        @Schema(description = "생성 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String createdAt,
        @Schema(description = "수정 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String updatedAt,
        @Schema(description = "처리 시각 (Asia/Seoul)", example = "2025-01-15 09:30:00")
                String processedAt) {}
