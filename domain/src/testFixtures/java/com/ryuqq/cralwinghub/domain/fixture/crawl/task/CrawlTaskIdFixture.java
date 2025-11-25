package com.ryuqq.cralwinghub.domain.fixture.crawl.task;

import com.ryuqq.crawlinghub.domain.crawl.task.identifier.CrawlTaskId;

/**
 * CrawlTaskId Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlTaskIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 할당된 ID 생성
     *
     * @return CrawlTaskId (value = 1L)
     */
    public static CrawlTaskId anAssignedId() {
        return CrawlTaskId.of(DEFAULT_ID);
    }

    /**
     * 특정 값으로 할당된 ID 생성
     *
     * @param value ID 값
     * @return CrawlTaskId
     */
    public static CrawlTaskId anAssignedId(Long value) {
        return CrawlTaskId.of(value);
    }

    /**
     * 미할당 ID 생성
     *
     * @return CrawlTaskId (value = null)
     */
    public static CrawlTaskId anUnassignedId() {
        return CrawlTaskId.unassigned();
    }

    private CrawlTaskIdFixture() {
        // Utility class
    }
}
