package com.ryuqq.cralwinghub.domain.fixture.schedule;

import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;

/**
 * CrawlSchedulerHistoryId Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlSchedulerHistoryIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 할당된 ID 생성
     *
     * @return CrawlSchedulerHistoryId (value = 1L)
     */
    public static CrawlSchedulerHistoryId anAssignedId() {
        return CrawlSchedulerHistoryId.of(DEFAULT_ID);
    }

    /**
     * 특정 값으로 할당된 ID 생성
     *
     * @param value ID 값
     * @return CrawlSchedulerHistoryId
     */
    public static CrawlSchedulerHistoryId anAssignedId(Long value) {
        return CrawlSchedulerHistoryId.of(value);
    }

    /**
     * 신규 생성용 ID (null)
     *
     * @return CrawlSchedulerHistoryId (value = null)
     */
    public static CrawlSchedulerHistoryId aNewId() {
        return CrawlSchedulerHistoryId.forNew();
    }

    private CrawlSchedulerHistoryIdFixture() {
        // Utility class
    }
}
