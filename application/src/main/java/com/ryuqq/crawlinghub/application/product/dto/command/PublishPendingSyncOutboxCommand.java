package com.ryuqq.crawlinghub.application.product.dto.command;

/**
 * CrawledProductSyncOutbox PENDING/FAILED 발행 Command
 *
 * @param batchSize 배치 처리 크기
 * @param maxRetryCount 최대 재시도 횟수 (이 횟수 이하의 FAILED만 대상)
 * @author development-team
 * @since 1.0.0
 */
public record PublishPendingSyncOutboxCommand(int batchSize, int maxRetryCount) {

    public PublishPendingSyncOutboxCommand {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize는 0보다 커야 합니다: " + batchSize);
        }
        if (maxRetryCount < 0) {
            throw new IllegalArgumentException("maxRetryCount는 0 이상이어야 합니다: " + maxRetryCount);
        }
    }

    public static PublishPendingSyncOutboxCommand of(int batchSize, int maxRetryCount) {
        return new PublishPendingSyncOutboxCommand(batchSize, maxRetryCount);
    }
}
