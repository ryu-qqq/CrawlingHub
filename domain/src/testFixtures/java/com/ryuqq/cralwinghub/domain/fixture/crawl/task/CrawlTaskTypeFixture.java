package com.ryuqq.cralwinghub.domain.fixture.crawl.task;

import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskType;

import java.util.List;

/**
 * CrawlTaskType Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlTaskTypeFixture {

    /**
     * 모든 유형 목록 반환
     *
     * @return 모든 CrawlTaskType 목록
     */
    public static List<CrawlTaskType> allTypes() {
        return List.of(CrawlTaskType.values());
    }

    /**
     * 기본 유형 반환
     *
     * @return META 유형
     */
    public static CrawlTaskType defaultType() {
        return CrawlTaskType.META;
    }

    private CrawlTaskTypeFixture() {
        // Utility class
    }
}
