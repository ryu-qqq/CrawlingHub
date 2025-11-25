package com.ryuqq.crawlinghub.application.crawl.task.dto.query;

/**
 * CrawlTask 단건 조회 Query DTO
 *
 * @param crawlTaskId CrawlTask ID (NotNull)
 * @author development-team
 * @since 1.0.0
 */
public record GetCrawlTaskQuery(
        Long crawlTaskId
) {
    public GetCrawlTaskQuery {
        if (crawlTaskId == null) {
            throw new IllegalArgumentException("crawlTaskId는 null일 수 없습니다.");
        }
    }
}
