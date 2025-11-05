package com.ryuqq.crawlinghub.domain.crawl.task;

/**
 * CrawlTask 식별자
 */
public record CrawlTaskId(Long value) {

    public CrawlTaskId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("CrawlTask ID는 양수여야 합니다");
        }
    }

    public static CrawlTaskId of(Long value) {
        return new CrawlTaskId(value);
    }
}
