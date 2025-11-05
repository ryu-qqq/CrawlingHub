package com.ryuqq.crawlinghub.application.task.command;

/**
 * 태스크 실패 처리 Command
 *
 * @param taskId     태스크 ID (필수)
 * @param error      에러 메시지 (필수)
 * @param statusCode HTTP 상태 코드 (nullable)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record TaskFailureCommand(
    Long taskId,
    String error,
    Integer statusCode
) {
    public TaskFailureCommand {
        if (taskId == null) {
            throw new IllegalArgumentException("태스크 ID는 필수입니다");
        }
        if (error == null || error.isBlank()) {
            throw new IllegalArgumentException("에러 메시지는 필수입니다");
        }
    }
}
