package com.ryuqq.crawlinghub.application.schedule.dto.command;

/**
 * PENDING 상태 아웃박스 처리 Command
 *
 * @param batchSize 배치 처리 크기
 * @param delaySeconds 생성 후 최소 대기 시간 (초)
 * @author development-team
 * @since 1.0.0
 */
public record ProcessPendingSchedulerOutboxCommand(int batchSize, int delaySeconds) {

    public ProcessPendingSchedulerOutboxCommand {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize는 0보다 커야 합니다: " + batchSize);
        }
        if (delaySeconds < 0) {
            throw new IllegalArgumentException("delaySeconds는 0 이상이어야 합니다: " + delaySeconds);
        }
    }

    public static ProcessPendingSchedulerOutboxCommand of(int batchSize, int delaySeconds) {
        return new ProcessPendingSchedulerOutboxCommand(batchSize, delaySeconds);
    }
}
