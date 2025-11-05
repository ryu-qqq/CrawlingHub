package com.ryuqq.crawlinghub.application.crawl.processing.dto.command;

import com.ryuqq.crawlinghub.application.task.command.ProcessTaskCommand;

/**
 * ProcessTaskCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class ProcessTaskCommandFixture {

    private static final Long DEFAULT_TASK_ID = 1L;
    private static final String DEFAULT_SQS_MESSAGE = "{\"taskId\":1,\"type\":\"MINI_SHOP\",\"url\":\"https://example.com\"}";

    /**
     * 기본 ProcessTaskCommand 생성
     *
     * @return ProcessTaskCommand
     */
    public static ProcessTaskCommand create() {
        return new ProcessTaskCommand(
            DEFAULT_TASK_ID,
            DEFAULT_SQS_MESSAGE
        );
    }

    /**
     * 특정 태스크 ID로 ProcessTaskCommand 생성
     *
     * @param taskId 태스크 ID
     * @return ProcessTaskCommand
     */
    public static ProcessTaskCommand createWithTaskId(Long taskId) {
        return new ProcessTaskCommand(
            taskId,
            DEFAULT_SQS_MESSAGE
        );
    }

    /**
     * 특정 SQS 메시지로 ProcessTaskCommand 생성
     *
     * @param sqsMessage SQS 메시지
     * @return ProcessTaskCommand
     */
    public static ProcessTaskCommand createWithSqsMessage(String sqsMessage) {
        return new ProcessTaskCommand(
            DEFAULT_TASK_ID,
            sqsMessage
        );
    }

    /**
     * 완전한 커스텀 ProcessTaskCommand 생성
     *
     * @param taskId     태스크 ID
     * @param sqsMessage SQS 메시지
     * @return ProcessTaskCommand
     */
    public static ProcessTaskCommand createCustom(
        Long taskId,
        String sqsMessage
    ) {
        return new ProcessTaskCommand(taskId, sqsMessage);
    }
}
