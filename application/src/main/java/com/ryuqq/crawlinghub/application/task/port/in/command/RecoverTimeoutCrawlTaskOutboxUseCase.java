package com.ryuqq.crawlinghub.application.task.port.in.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverTimeoutCrawlTaskOutboxCommand;

/**
 * 타임아웃 CrawlTask 아웃박스 복구 UseCase
 *
 * <p><strong>용도</strong>: PROCESSING 상태에서 일정 시간 초과된 좀비 아웃박스를 PENDING으로 복원
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverTimeoutCrawlTaskOutboxUseCase {

    /**
     * 타임아웃된 아웃박스를 PENDING으로 복구
     *
     * @param command 복구 명령 (batchSize, timeoutSeconds)
     * @return 스케줄러 배치 처리 결과
     */
    SchedulerBatchProcessingResult execute(RecoverTimeoutCrawlTaskOutboxCommand command);
}
