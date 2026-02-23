package com.ryuqq.crawlinghub.application.schedule.port.in.query;

import com.ryuqq.crawlinghub.application.schedule.dto.query.CrawlSchedulerSearchParams;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerPageResult;

/**
 * 크롤 스케줄러 오프셋 기반 다건 조회 UseCase (Port In)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchCrawlSchedulerByOffsetUseCase {

    /**
     * 크롤 스케줄러 다건 조회
     *
     * @param params 검색 파라미터
     * @return 스케줄러 페이지 결과
     */
    CrawlSchedulerPageResult execute(CrawlSchedulerSearchParams params);
}
