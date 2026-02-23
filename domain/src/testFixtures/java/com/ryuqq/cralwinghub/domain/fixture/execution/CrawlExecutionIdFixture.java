package com.ryuqq.cralwinghub.domain.fixture.execution;

import com.ryuqq.crawlinghub.domain.execution.id.CrawlExecutionId;

/**
 * CrawlExecutionId Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlExecutionIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 할당된 ID 생성
     *
     * @return CrawlExecutionId (value = 1L)
     */
    public static CrawlExecutionId anAssignedId() {
        return CrawlExecutionId.of(DEFAULT_ID);
    }

    /**
     * 특정 값으로 할당된 ID 생성
     *
     * @param value ID 값
     * @return CrawlExecutionId
     */
    public static CrawlExecutionId anAssignedId(Long value) {
        return CrawlExecutionId.of(value);
    }

    /**
     * 미할당 ID 생성
     *
     * @return CrawlExecutionId (value = null)
     */
    public static CrawlExecutionId anUnassignedId() {
        return CrawlExecutionId.forNew();
    }

    private CrawlExecutionIdFixture() {
        // Utility class
    }
}
