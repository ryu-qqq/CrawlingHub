package com.ryuqq.cralwinghub.domain.fixture.schedule;

import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;

/**
 * CrawlSchedulerId Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlSchedulerIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 할당된 ID 생성
     *
     * @return CrawlSchedulerId (value = 1L)
     */
    public static CrawlSchedulerId anAssignedId() {
        return CrawlSchedulerId.of(DEFAULT_ID);
    }

    /**
     * 특정 값으로 할당된 ID 생성
     *
     * @param value ID 값
     * @return CrawlSchedulerId
     */
    public static CrawlSchedulerId anAssignedId(Long value) {
        return CrawlSchedulerId.of(value);
    }

    /**
     * 신규 생성용 ID (null)
     *
     * @return CrawlSchedulerId (value = null)
     */
    public static CrawlSchedulerId aNewId() {
        return CrawlSchedulerId.forNew();
    }

    private CrawlSchedulerIdFixture() {
        // Utility class
    }
}
