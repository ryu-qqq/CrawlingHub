package com.ryuqq.crawlinghub.application.task.port.in.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverFailedCrawlTaskOutboxCommand;

/**
 * FAILED 상태 CrawlTask 아웃박스 복구 UseCase
 *
 * <p>FAILED 상태에서 일정 시간 경과한 아웃박스를 PENDING으로 복원하여 자동 재처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverFailedCrawlTaskOutboxUseCase {

    SchedulerBatchProcessingResult execute(RecoverFailedCrawlTaskOutboxCommand command);
}
