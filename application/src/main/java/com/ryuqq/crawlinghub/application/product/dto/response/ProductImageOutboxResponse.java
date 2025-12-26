package com.ryuqq.crawlinghub.application.product.dto.response;

import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;

/**
 * ProductImageOutbox 응답 DTO
 *
 * @param id Outbox ID
 * @param crawledProductImageId CrawledProductImage ID
 * @param idempotencyKey 멱등성 키
 * @param status 상태
 * @param retryCount 재시도 횟수
 * @param errorMessage 에러 메시지 (nullable)
 * @param canRetry 재시도 가능 여부
 * @param createdAt 생성 시각
 * @param processedAt 처리 시각 (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record ProductImageOutboxResponse(
        Long id,
        Long crawledProductImageId,
        String idempotencyKey,
        ProductOutboxStatus status,
        int retryCount,
        String errorMessage,
        boolean canRetry,
        Instant createdAt,
        Instant processedAt) {

    public static ProductImageOutboxResponse from(ProductImageOutbox outbox) {
        return new ProductImageOutboxResponse(
                outbox.getId(),
                outbox.getCrawledProductImageId(),
                outbox.getIdempotencyKey(),
                outbox.getStatus(),
                outbox.getRetryCount(),
                outbox.getErrorMessage(),
                outbox.canRetry(),
                outbox.getCreatedAt(),
                outbox.getProcessedAt());
    }
}
