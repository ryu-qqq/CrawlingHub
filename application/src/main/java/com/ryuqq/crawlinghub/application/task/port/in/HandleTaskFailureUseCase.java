package com.ryuqq.crawlinghub.application.task.port.in;

import com.ryuqq.crawlinghub.application.task.command.TaskFailureCommand;

/**
 * 태스크 실패 처리 UseCase
 *
 * <p>태스크 실패 시 재시도 또는 DLQ 이동을 결정합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface HandleTaskFailureUseCase {

    /**
     * 태스크 실패 처리
     *
     * @param command 실패 정보
     */
    void execute(TaskFailureCommand command);
}
