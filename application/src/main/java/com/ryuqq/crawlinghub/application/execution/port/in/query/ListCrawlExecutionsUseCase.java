package com.ryuqq.crawlinghub.application.execution.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;

/**
 * CrawlExecution 목록 조회 UseCase (Port In - Query)
 *
 * <p>CrawlTask/Schedule/Seller별 CrawlExecution 목록 페이징 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ListCrawlExecutionsUseCase {

    /**
     * CrawlExecution 목록 조회 실행
     *
     * @param query 조회 쿼리 (crawlTaskId, crawlSchedulerId, sellerId, status, from, to, page, size)
     * @return CrawlExecution 페이징 응답
     */
    PageResponse<CrawlExecutionResponse> execute(ListCrawlExecutionsQuery query);
}
