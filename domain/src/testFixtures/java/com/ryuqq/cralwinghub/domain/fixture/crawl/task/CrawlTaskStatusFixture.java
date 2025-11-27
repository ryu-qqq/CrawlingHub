package com.ryuqq.cralwinghub.domain.fixture.crawl.task;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.List;

/**
 * CrawlTaskStatus Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlTaskStatusFixture {

    /**
     * 진행 중 상태 목록 반환
     *
     * @return WAITING, PUBLISHED, RUNNING 목록
     */
    public static List<CrawlTaskStatus> inProgressStatuses() {
        return List.of(CrawlTaskStatus.WAITING, CrawlTaskStatus.PUBLISHED, CrawlTaskStatus.RUNNING);
    }

    /**
     * 종료 상태 목록 반환
     *
     * @return SUCCESS, FAILED 목록
     */
    public static List<CrawlTaskStatus> terminalStatuses() {
        return List.of(CrawlTaskStatus.SUCCESS, CrawlTaskStatus.FAILED);
    }

    /**
     * 재시도 가능 상태 목록 반환
     *
     * @return FAILED, TIMEOUT 목록
     */
    public static List<CrawlTaskStatus> retryableStatuses() {
        return List.of(CrawlTaskStatus.FAILED, CrawlTaskStatus.TIMEOUT);
    }

    private CrawlTaskStatusFixture() {
        // Utility class
    }
}
