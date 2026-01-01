package com.ryuqq.crawlinghub.application.product.dto.response;

import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;

/**
 * ProductImageOutbox + CrawledProductImage 응답 DTO
 *
 * <p>Outbox 정보와 이미지 정보를 함께 제공하는 응답 DTO입니다.
 *
 * @param id Outbox ID
 * @param crawledProductImageId CrawledProductImage ID
 * @param idempotencyKey 멱등성 키
 * @param status 상태
 * @param retryCount 재시도 횟수
 * @param errorMessage 에러 메시지 (nullable)
 * @param canRetry 재시도 가능 여부
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param processedAt 처리 시각 (nullable)
 * @param crawledProductId CrawledProduct ID (이미지 정보)
 * @param originalUrl 원본 이미지 URL (이미지 정보)
 * @param s3Url S3 업로드 URL (이미지 정보, nullable)
 * @param imageType 이미지 타입 (이미지 정보)
 * @author development-team
 * @since 1.0.0
 */
public record ProductImageOutboxWithImageResponse(
        Long id,
        Long crawledProductImageId,
        String idempotencyKey,
        ProductOutboxStatus status,
        int retryCount,
        String errorMessage,
        boolean canRetry,
        Instant createdAt,
        Instant updatedAt,
        Instant processedAt,
        Long crawledProductId,
        String originalUrl,
        String s3Url,
        ImageType imageType) {

    /**
     * 정적 팩토리 메서드
     *
     * @param id Outbox ID
     * @param crawledProductImageId CrawledProductImage ID
     * @param idempotencyKey 멱등성 키
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @param processedAt 처리 시각
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 이미지 URL
     * @param s3Url S3 업로드 URL
     * @param imageType 이미지 타입
     * @return ProductImageOutboxWithImageResponse
     */
    public static ProductImageOutboxWithImageResponse of(
            Long id,
            Long crawledProductImageId,
            String idempotencyKey,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            Instant createdAt,
            Instant updatedAt,
            Instant processedAt,
            Long crawledProductId,
            String originalUrl,
            String s3Url,
            ImageType imageType) {
        boolean canRetry = retryCount < 3;
        return new ProductImageOutboxWithImageResponse(
                id,
                crawledProductImageId,
                idempotencyKey,
                status,
                retryCount,
                errorMessage,
                canRetry,
                createdAt,
                updatedAt,
                processedAt,
                crawledProductId,
                originalUrl,
                s3Url,
                imageType);
    }
}
