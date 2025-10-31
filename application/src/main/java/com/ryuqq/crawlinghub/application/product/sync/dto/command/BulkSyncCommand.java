package com.ryuqq.crawlinghub.application.product.sync.dto.command;

import java.time.LocalDateTime;

/**
 * 대량 동기화 Command
 *
 * <p>특정 조건에 맞는 상품들을 대량으로 동기화합니다.
 *
 * @param sellerId 셀러 ID (null이면 전체)
 * @param changedAfter 이 시간 이후 변경된 상품만 (null이면 전체)
 * @param batchSize 한 번에 처리할 배치 크기
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record BulkSyncCommand(
    Long sellerId,
    LocalDateTime changedAfter,
    Integer batchSize
) {
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int MAX_BATCH_SIZE = 1000;

    public BulkSyncCommand {
        if (batchSize == null) {
            batchSize = DEFAULT_BATCH_SIZE;
        }
        if (batchSize <= 0 || batchSize > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException(
                String.format("배치 크기는 1~%d 사이여야 합니다", MAX_BATCH_SIZE)
            );
        }
    }

    /**
     * 전체 동기화 (기본 배치 크기)
     */
    public static BulkSyncCommand all() {
        return new BulkSyncCommand(null, null, DEFAULT_BATCH_SIZE);
    }

    /**
     * 특정 셀러 동기화
     */
    public static BulkSyncCommand forSeller(Long sellerId) {
        return new BulkSyncCommand(sellerId, null, DEFAULT_BATCH_SIZE);
    }

    /**
     * 최근 변경 상품 동기화
     */
    public static BulkSyncCommand changedAfter(LocalDateTime after) {
        return new BulkSyncCommand(null, after, DEFAULT_BATCH_SIZE);
    }

    /**
     * 셀러 필터 존재 여부
     */
    public boolean hasSellerFilter() {
        return sellerId != null;
    }

    /**
     * 시간 필터 존재 여부
     */
    public boolean hasTimeFilter() {
        return changedAfter != null;
    }
}
