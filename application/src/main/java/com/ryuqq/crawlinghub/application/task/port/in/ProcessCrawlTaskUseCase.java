package com.ryuqq.crawlinghub.application.task.port.in;

import com.ryuqq.crawlinghub.application.task.command.ProcessTaskCommand;

/**
 * 크롤링 태스크 처리 UseCase (SQS Consumer용)
 *
 * <p>SQS 메시지를 받아서 실제 크롤링을 수행합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface ProcessCrawlTaskUseCase {

    /**
     * 태스크 처리 (크롤링 실행)
     *
     * @param command SQS 메시지
     */
    void execute(ProcessTaskCommand command);
}
