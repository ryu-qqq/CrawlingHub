package com.ryuqq.crawlinghub.application.schedule.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;

/**
 * 크롤 스케줄러 다건 조회 UseCase (Port In).
 *
 * <p><strong>조회 시나리오</strong>:
 *
 * <ul>
 *   <li>어드민 화면: 전체 스케줄러 리스트 조회
 *   <li>셀러 상세: 셀러별 스케줄러 리스트 조회
 * </ul>
 */
public interface SearchCrawlSchedulesUseCase {

    /**
     * 크롤 스케줄러 다건 조회
     *
     * @param query 조회 조건 (sellerId, status, page, size)
     * @return 스케줄러 리스트 (PageResponse)
     */
    PageResponse<CrawlSchedulerResponse> execute(SearchCrawlSchedulersQuery query);
}
