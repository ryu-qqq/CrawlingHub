package com.ryuqq.crawlinghub.adapter.out.persistence.image.dto;

import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.LocalDateTime;

/**
 * ProductImageOutbox + CrawledProductImage JOIN 결과 DTO
 *
 * <p>ProductImageOutbox 조회 시 이미지 정보를 함께 반환하기 위한 QueryDSL Projection DTO입니다.
 *
 * @param id Outbox ID
 * @param crawledProductImageId CrawledProductImage ID
 * @param idempotencyKey 멱등성 키
 * @param status 상태
 * @param retryCount 재시도 횟수
 * @param errorMessage 에러 메시지 (nullable)
 * @param createdAt 생성 시각
 * @param processedAt 처리 시각 (nullable)
 * @param crawledProductId CrawledProduct ID (이미지 정보)
 * @param originalUrl 원본 이미지 URL (이미지 정보)
 * @param s3Url S3 업로드 URL (이미지 정보, nullable)
 * @param imageType 이미지 타입 (이미지 정보)
 * @author development-team
 * @since 1.0.0
 */
public record ProductImageOutboxWithImageDto(
        Long id,
        Long crawledProductImageId,
        String idempotencyKey,
        ProductOutboxStatus status,
        int retryCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        Long crawledProductId,
        String originalUrl,
        String s3Url,
        ImageType imageType) {

    /**
     * 재시도 가능 여부 확인
     *
     * @return 재시도 가능하면 true
     */
    public boolean canRetry() {
        return retryCount < 3;
    }
}
