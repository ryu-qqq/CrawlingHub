package com.ryuqq.crawlinghub.application.schedule.dto.command;

/**
 * 타임아웃 아웃박스 복구 Command
 *
 * @param batchSize 배치 처리 크기
 * @param timeoutSeconds PROCESSING 상태 타임아웃 기준 (초)
 * @author development-team
 * @since 1.0.0
 */
public record RecoverTimeoutSchedulerOutboxCommand(int batchSize, long timeoutSeconds) {

    public RecoverTimeoutSchedulerOutboxCommand {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize는 0보다 커야 합니다: " + batchSize);
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds는 0보다 커야 합니다: " + timeoutSeconds);
        }
    }

    public static RecoverTimeoutSchedulerOutboxCommand of(int batchSize, long timeoutSeconds) {
        return new RecoverTimeoutSchedulerOutboxCommand(batchSize, timeoutSeconds);
    }
}
