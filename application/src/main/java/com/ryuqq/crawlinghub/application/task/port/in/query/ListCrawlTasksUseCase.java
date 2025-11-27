package com.ryuqq.crawlinghub.application.task.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;

/**
 * CrawlTask 목록 조회 UseCase (Port In - Query)
 *
 * <p>Schedule ID로 CrawlTask 목록 페이징 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ListCrawlTasksUseCase {

    /**
     * CrawlTask 목록 조회 실행
     *
     * @param query 조회 쿼리 (crawlSchedulerId, status, page, size)
     * @return CrawlTask 페이징 응답
     */
    PageResponse<CrawlTaskResponse> execute(ListCrawlTasksQuery query);
}
