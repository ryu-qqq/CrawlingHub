package com.ryuqq.crawlinghub.application.product.dto.command;

public record RecoverFailedProductSyncOutboxCommand(int batchSize, int delaySeconds) {
    public RecoverFailedProductSyncOutboxCommand {
        if (batchSize <= 0)
            throw new IllegalArgumentException("batchSize는 0보다 커야 합니다: " + batchSize);
        if (delaySeconds <= 0)
            throw new IllegalArgumentException("delaySeconds는 0보다 커야 합니다: " + delaySeconds);
    }

    public static RecoverFailedProductSyncOutboxCommand of(int batchSize, int delaySeconds) {
        return new RecoverFailedProductSyncOutboxCommand(batchSize, delaySeconds);
    }
}
