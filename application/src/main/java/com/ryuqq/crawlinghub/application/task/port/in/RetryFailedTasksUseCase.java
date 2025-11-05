package com.ryuqq.crawlinghub.application.crawl.processing.port.in;

import com.ryuqq.crawlinghub.application.crawl.processing.dto.command.RetryTasksCommand;

/**
 * 실패 태스크 재시도 UseCase
 *
 * <p>실패한 태스크들을 일괄 재시도합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface RetryFailedTasksUseCase {

    /**
     * 실패 태스크 재시도
     *
     * @param command 재시도 조건
     */
    void execute(RetryTasksCommand command);
}
