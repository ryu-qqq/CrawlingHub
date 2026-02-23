package com.ryuqq.crawlinghub.application.task.dto.command;

/**
 * FAILED CrawlTask 아웃박스 복구 Command
 *
 * @param batchSize 배치 처리 크기
 * @param delaySeconds FAILED 후 경과해야 할 최소 시간 (초)
 * @author development-team
 * @since 1.0.0
 */
public record RecoverFailedCrawlTaskOutboxCommand(int batchSize, int delaySeconds) {

    public RecoverFailedCrawlTaskOutboxCommand {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize는 0보다 커야 합니다: " + batchSize);
        }
        if (delaySeconds <= 0) {
            throw new IllegalArgumentException("delaySeconds는 0보다 커야 합니다: " + delaySeconds);
        }
    }

    public static RecoverFailedCrawlTaskOutboxCommand of(int batchSize, int delaySeconds) {
        return new RecoverFailedCrawlTaskOutboxCommand(batchSize, delaySeconds);
    }
}
