package com.ryuqq.crawlinghub.application.task.command;

import com.ryuqq.crawlinghub.domain.task.TaskType;

/**
 * 실패 태스크 재시도 Command
 *
 * @param sellerId 셀러 ID (필수)
 * @param taskType 태스크 유형 (nullable, null이면 모든 유형)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record RetryTasksCommand(
    Long sellerId,
    TaskType taskType
) {
    public RetryTasksCommand {
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 필수입니다");
        }
    }
}
