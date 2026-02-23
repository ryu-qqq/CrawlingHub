package com.ryuqq.crawlinghub.application.task.dto.command;

/**
 * RUNNING 고아 CrawlTask 복구 Command
 *
 * <p><strong>용도</strong>: RUNNING 상태에서 일정 시간 이상 머물러있는 고아 CrawlTask 복구 요청
 *
 * @param batchSize 한 번에 처리할 최대 건수
 * @param timeoutSeconds RUNNING 상태 유지 시간 기준 (초)
 * @author development-team
 * @since 1.0.0
 */
public record RecoverStuckCrawlTaskCommand(int batchSize, long timeoutSeconds) {

    public RecoverStuckCrawlTaskCommand {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize는 0보다 커야 합니다.");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds는 0보다 커야 합니다.");
        }
    }

    public static RecoverStuckCrawlTaskCommand of(int batchSize, long timeoutSeconds) {
        return new RecoverStuckCrawlTaskCommand(batchSize, timeoutSeconds);
    }
}
