package com.ryuqq.crawlinghub.application.product.dto.response;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;

/**
 * ProductSyncOutbox 응답 DTO
 *
 * @param id Outbox ID
 * @param crawledProductId CrawledProduct ID
 * @param sellerId 셀러 ID
 * @param itemNo 상품 번호
 * @param syncType 동기화 타입 (CREATE, UPDATE)
 * @param idempotencyKey 멱등성 키
 * @param externalProductId 외부 상품 ID (nullable)
 * @param status 상태
 * @param retryCount 재시도 횟수
 * @param errorMessage 에러 메시지 (nullable)
 * @param canRetry 재시도 가능 여부
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param processedAt 처리 시각 (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record ProductSyncOutboxResponse(
        Long id,
        Long crawledProductId,
        Long sellerId,
        long itemNo,
        String syncType,
        String idempotencyKey,
        Long externalProductId,
        ProductOutboxStatus status,
        int retryCount,
        String errorMessage,
        boolean canRetry,
        Instant createdAt,
        Instant updatedAt,
        Instant processedAt) {

    /**
     * CrawledProductSyncOutbox Domain → ProductSyncOutboxResponse 변환
     *
     * @param domain CrawledProductSyncOutbox Domain
     * @return ProductSyncOutboxResponse
     */
    public static ProductSyncOutboxResponse from(CrawledProductSyncOutbox domain) {
        boolean canRetry = domain.getRetryCount() < 3;
        return new ProductSyncOutboxResponse(
                domain.getId(),
                domain.getCrawledProductIdValue(),
                domain.getSellerIdValue(),
                domain.getItemNo(),
                domain.getSyncType().name(),
                domain.getIdempotencyKey(),
                domain.getExternalProductId(),
                domain.getStatus(),
                domain.getRetryCount(),
                domain.getErrorMessage(),
                canRetry,
                domain.getCreatedAt(),
                null, // updatedAt: domain 모델에 미존재, 향후 추가 예정
                domain.getProcessedAt());
    }
}
