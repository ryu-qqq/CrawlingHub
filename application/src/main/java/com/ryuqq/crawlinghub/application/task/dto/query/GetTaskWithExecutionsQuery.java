package com.ryuqq.crawlinghub.application.task.dto.query;

/**
 * Task 상세 + Execution 이력 조회 Query DTO
 *
 * @param crawlTaskId Task ID
 * @param executionLimit Execution 이력 조회 개수 (기본값: 10)
 * @author development-team
 * @since 1.0.0
 */
public record GetTaskWithExecutionsQuery(Long crawlTaskId, int executionLimit) {

    private static final int DEFAULT_EXECUTION_LIMIT = 10;

    public GetTaskWithExecutionsQuery(Long crawlTaskId) {
        this(crawlTaskId, DEFAULT_EXECUTION_LIMIT);
    }

    public static GetTaskWithExecutionsQuery of(Long crawlTaskId) {
        return new GetTaskWithExecutionsQuery(crawlTaskId, DEFAULT_EXECUTION_LIMIT);
    }

    public static GetTaskWithExecutionsQuery of(Long crawlTaskId, int executionLimit) {
        return new GetTaskWithExecutionsQuery(crawlTaskId, executionLimit);
    }
}
