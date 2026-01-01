package com.ryuqq.crawlinghub.application.task.port.in.command;

import com.ryuqq.crawlinghub.application.task.dto.response.RepublishResultResponse;

/**
 * Outbox 재발행 UseCase
 *
 * <p>특정 CrawlTask의 Outbox를 SQS로 다시 발행합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RepublishOutboxUseCase {

    /**
     * Outbox를 SQS로 재발행합니다.
     *
     * @param crawlTaskId 재발행할 Task ID
     * @return 재발행 결과
     */
    RepublishResultResponse republish(Long crawlTaskId);
}
