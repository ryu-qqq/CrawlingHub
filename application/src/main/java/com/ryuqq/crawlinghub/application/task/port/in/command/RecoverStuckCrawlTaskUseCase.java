package com.ryuqq.crawlinghub.application.task.port.in.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverStuckCrawlTaskCommand;

/**
 * RUNNING 고아 CrawlTask 복구 UseCase
 *
 * <p><strong>용도</strong>: RUNNING 상태에서 일정 시간 이상 머물러있는 고아 CrawlTask를 자동 복구
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverStuckCrawlTaskUseCase {

    /**
     * RUNNING 고아 CrawlTask 복구 실행
     *
     * @param command 복구 명령 (batchSize, timeoutSeconds)
     * @return 배치 처리 결과
     */
    SchedulerBatchProcessingResult execute(RecoverStuckCrawlTaskCommand command);
}
