package com.ryuqq.crawlinghub.domain.crawl.schedule;

import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;

/**
 * CrawlScheduleId Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class CrawlScheduleIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 CrawlScheduleId 생성
     *
     * @return CrawlScheduleId
     */
    public static CrawlScheduleId create() {
        return CrawlScheduleId.of(DEFAULT_ID);
    }

    /**
     * 지정된 ID로 CrawlScheduleId 생성
     *
     * @param id ID 값
     * @return CrawlScheduleId
     */
    public static CrawlScheduleId createWithId(Long id) {
        return CrawlScheduleId.of(id);
    }

    /**
     * null ID로 CrawlScheduleId 생성 (신규 엔티티용)
     *
     * @return null
     */
    public static CrawlScheduleId createNull() {
        return null;
    }
}
