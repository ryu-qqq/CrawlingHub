package com.ryuqq.crawlinghub.domain.crawl.schedule;

/**
 * CrawlSchedule 식별자
 */
public record CrawlScheduleId(Long value) {

    public CrawlScheduleId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("CrawlSchedule ID는 양수여야 합니다");
        }
    }

    public static CrawlScheduleId of(Long value) {
        return new CrawlScheduleId(value);
    }
}
