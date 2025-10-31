package com.ryuqq.crawlinghub.application.crawl.processing.dto.command;

import com.ryuqq.crawlinghub.domain.crawl.task.TaskType;

/**
 * RetryTasksCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class RetryTasksCommandFixture {

    private static final Long DEFAULT_SELLER_ID = 1L;
    private static final TaskType DEFAULT_TASK_TYPE = TaskType.MINI_SHOP;

    /**
     * 기본 RetryTasksCommand 생성 (모든 태스크 유형)
     *
     * @return RetryTasksCommand
     */
    public static RetryTasksCommand create() {
        return new RetryTasksCommand(
            DEFAULT_SELLER_ID,
            null
        );
    }

    /**
     * 특정 태스크 유형으로 RetryTasksCommand 생성
     *
     * @return RetryTasksCommand
     */
    public static RetryTasksCommand createWithTaskType() {
        return new RetryTasksCommand(
            DEFAULT_SELLER_ID,
            DEFAULT_TASK_TYPE
        );
    }

    /**
     * 특정 셀러 ID로 RetryTasksCommand 생성
     *
     * @param sellerId 셀러 ID
     * @return RetryTasksCommand
     */
    public static RetryTasksCommand createWithSellerId(Long sellerId) {
        return new RetryTasksCommand(
            sellerId,
            null
        );
    }

    /**
     * 완전한 커스텀 RetryTasksCommand 생성
     *
     * @param sellerId 셀러 ID
     * @param taskType 태스크 유형
     * @return RetryTasksCommand
     */
    public static RetryTasksCommand createCustom(
        Long sellerId,
        TaskType taskType
    ) {
        return new RetryTasksCommand(sellerId, taskType);
    }
}
