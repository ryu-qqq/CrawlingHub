package com.ryuqq.crawlinghub.application.product.sync.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 대량 동기화 결과 Response
 *
 * @param totalCount 총 대상 상품 수
 * @param successCount 성공 개수
 * @param failureCount 실패 개수
 * @param startedAt 시작 시간
 * @param completedAt 완료 시간
 * @param failedProducts 실패한 상품 ID 목록
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record BulkSyncResponse(
    int totalCount,
    int successCount,
    int failureCount,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    List<Long> failedProducts
) {
    /**
     * 성공률 계산
     */
    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 처리 시간 (초)
     */
    public long getProcessingTimeSeconds() {
        return java.time.Duration.between(startedAt, completedAt).getSeconds();
    }
}
