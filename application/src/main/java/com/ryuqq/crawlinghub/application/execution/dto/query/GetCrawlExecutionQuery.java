package com.ryuqq.crawlinghub.application.execution.dto.query;

/**
 * CrawlExecution 단건 조회 Query DTO
 *
 * @param crawlExecutionId CrawlExecution ID (NotNull)
 * @author development-team
 * @since 1.0.0
 */
public record GetCrawlExecutionQuery(Long crawlExecutionId) {
    public GetCrawlExecutionQuery {
        if (crawlExecutionId == null) {
            throw new IllegalArgumentException("crawlExecutionId는 null일 수 없습니다.");
        }
    }
}
