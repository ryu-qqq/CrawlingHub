package com.ryuqq.cralwinghub.domain.fixture.schedule;

import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerOutBoxId;

/**
 * CrawlSchedulerOutBoxId 테스트 Fixture
 *
 * <p>Object Mother 패턴: 테스트용 CrawlSchedulerOutBoxId 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlSchedulerOutBoxIdFixture {

    private CrawlSchedulerOutBoxIdFixture() {
        throw new UnsupportedOperationException("Fixture 클래스입니다.");
    }

    /**
     * 할당된 ID (DB 저장 후) 생성
     *
     * @return 할당된 CrawlSchedulerOutBoxId
     */
    public static CrawlSchedulerOutBoxId anAssignedId() {
        return CrawlSchedulerOutBoxId.of(1L);
    }

    /**
     * 특정 값으로 할당된 ID 생성
     *
     * @param value ID 값
     * @return 할당된 CrawlSchedulerOutBoxId
     */
    public static CrawlSchedulerOutBoxId anAssignedId(Long value) {
        return CrawlSchedulerOutBoxId.of(value);
    }
}
