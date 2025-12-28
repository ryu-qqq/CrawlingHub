package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response;

import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxWithImageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * ProductImageOutbox API 응답 (이미지 정보 포함)
 *
 * @param id Outbox ID
 * @param crawledProductImageId CrawledProductImage ID
 * @param idempotencyKey 멱등성 키
 * @param status 상태 (PENDING, PROCESSING, COMPLETED, FAILED)
 * @param retryCount 재시도 횟수
 * @param errorMessage 에러 메시지 (nullable)
 * @param canRetry 재시도 가능 여부
 * @param createdAt 생성 시각
 * @param processedAt 처리 시각 (nullable)
 * @param crawledProductId CrawledProduct ID (이미지 정보)
 * @param originalUrl 원본 이미지 URL (이미지 정보)
 * @param s3Url S3 업로드 URL (이미지 정보, nullable)
 * @param imageType 이미지 타입 (이미지 정보)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "상품 이미지 Outbox 응답 (이미지 정보 포함)")
public record ProductImageOutboxApiResponse(
        @Schema(description = "Outbox ID", example = "1") Long id,
        @Schema(description = "CrawledProductImage ID", example = "100") Long crawledProductImageId,
        @Schema(description = "멱등성 키", example = "image-100-1234567890") String idempotencyKey,
        @Schema(description = "상태", example = "PENDING") String status,
        @Schema(description = "재시도 횟수", example = "0") int retryCount,
        @Schema(description = "에러 메시지") String errorMessage,
        @Schema(description = "재시도 가능 여부", example = "true") boolean canRetry,
        @Schema(description = "생성 시각", example = "2024-01-15T10:30:00") LocalDateTime createdAt,
        @Schema(description = "처리 시각") LocalDateTime processedAt,
        @Schema(description = "CrawledProduct ID", example = "50") Long crawledProductId,
        @Schema(description = "원본 이미지 URL", example = "https://example.com/image.jpg")
                String originalUrl,
        @Schema(description = "S3 업로드 URL") String s3Url,
        @Schema(description = "이미지 타입", example = "MAIN") String imageType) {

    public static ProductImageOutboxApiResponse from(ProductImageOutboxWithImageResponse response) {
        return new ProductImageOutboxApiResponse(
                response.id(),
                response.crawledProductImageId(),
                response.idempotencyKey(),
                response.status().name(),
                response.retryCount(),
                response.errorMessage(),
                response.canRetry(),
                response.createdAt(),
                response.processedAt(),
                response.crawledProductId(),
                response.originalUrl(),
                response.s3Url(),
                response.imageType() != null ? response.imageType().name() : null);
    }
}
