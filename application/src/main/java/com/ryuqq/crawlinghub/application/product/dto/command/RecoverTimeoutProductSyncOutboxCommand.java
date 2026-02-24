package com.ryuqq.crawlinghub.application.product.dto.command;

public record RecoverTimeoutProductSyncOutboxCommand(int batchSize, long timeoutSeconds) {
    public RecoverTimeoutProductSyncOutboxCommand {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize는 0보다 커야 합니다: " + batchSize);
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds는 0보다 커야 합니다: " + timeoutSeconds);
        }
    }

    public static RecoverTimeoutProductSyncOutboxCommand of(int batchSize, long timeoutSeconds) {
        return new RecoverTimeoutProductSyncOutboxCommand(batchSize, timeoutSeconds);
    }
}
