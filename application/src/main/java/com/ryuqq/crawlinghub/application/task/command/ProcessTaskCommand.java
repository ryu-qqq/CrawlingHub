package com.ryuqq.crawlinghub.application.task.command;

/**
 * 태스크 처리 Command (SQS Consumer용)
 *
 * @param taskId     태스크 ID (필수)
 * @param sqsMessage SQS 메시지 내용 (JSON) (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record ProcessTaskCommand(
    Long taskId,
    String sqsMessage
) {
    public ProcessTaskCommand {
        if (taskId == null) {
            throw new IllegalArgumentException("태스크 ID는 필수입니다");
        }
        if (sqsMessage == null || sqsMessage.isBlank()) {
            throw new IllegalArgumentException("SQS 메시지는 필수입니다");
        }
    }
}
