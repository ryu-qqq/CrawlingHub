package com.ryuqq.crawlinghub.application.crawl.processing.dto.command;

import com.ryuqq.crawlinghub.application.task.command.TaskFailureCommand;

/**
 * TaskFailureCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class TaskFailureCommandFixture {

    private static final Long DEFAULT_TASK_ID = 1L;
    private static final String DEFAULT_ERROR = "Connection timeout";
    private static final Integer DEFAULT_STATUS_CODE = 500;

    /**
     * 기본 TaskFailureCommand 생성
     *
     * @return TaskFailureCommand
     */
    public static TaskFailureCommand create() {
        return new TaskFailureCommand(
            DEFAULT_TASK_ID,
            DEFAULT_ERROR,
            DEFAULT_STATUS_CODE
        );
    }

    /**
     * 상태 코드 없이 TaskFailureCommand 생성
     *
     * @return TaskFailureCommand
     */
    public static TaskFailureCommand createWithoutStatusCode() {
        return new TaskFailureCommand(
            DEFAULT_TASK_ID,
            DEFAULT_ERROR,
            null
        );
    }

    /**
     * 특정 태스크 ID로 TaskFailureCommand 생성
     *
     * @param taskId 태스크 ID
     * @return TaskFailureCommand
     */
    public static TaskFailureCommand createWithTaskId(Long taskId) {
        return new TaskFailureCommand(
            taskId,
            DEFAULT_ERROR,
            DEFAULT_STATUS_CODE
        );
    }

    /**
     * 특정 에러 메시지로 TaskFailureCommand 생성
     *
     * @param error 에러 메시지
     * @return TaskFailureCommand
     */
    public static TaskFailureCommand createWithError(String error) {
        return new TaskFailureCommand(
            DEFAULT_TASK_ID,
            error,
            DEFAULT_STATUS_CODE
        );
    }

    /**
     * 특정 상태 코드로 TaskFailureCommand 생성
     *
     * @param statusCode HTTP 상태 코드
     * @return TaskFailureCommand
     */
    public static TaskFailureCommand createWithStatusCode(Integer statusCode) {
        return new TaskFailureCommand(
            DEFAULT_TASK_ID,
            DEFAULT_ERROR,
            statusCode
        );
    }

    /**
     * 완전한 커스텀 TaskFailureCommand 생성
     *
     * @param taskId     태스크 ID
     * @param error      에러 메시지
     * @param statusCode HTTP 상태 코드
     * @return TaskFailureCommand
     */
    public static TaskFailureCommand createCustom(
        Long taskId,
        String error,
        Integer statusCode
    ) {
        return new TaskFailureCommand(taskId, error, statusCode);
    }
}
