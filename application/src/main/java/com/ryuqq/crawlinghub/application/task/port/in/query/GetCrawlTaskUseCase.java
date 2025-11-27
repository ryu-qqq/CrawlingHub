package com.ryuqq.crawlinghub.application.task.port.in.query;

import com.ryuqq.crawlinghub.application.task.dto.query.GetCrawlTaskQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;

/**
 * CrawlTask 단건 조회 UseCase (Port In - Query)
 *
 * <p>Task ID로 CrawlTask 상세 정보 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetCrawlTaskUseCase {

    /**
     * CrawlTask 단건 조회 실행
     *
     * @param query 조회 쿼리 (crawlTaskId)
     * @return CrawlTask 상세 응답
     */
    CrawlTaskDetailResponse execute(GetCrawlTaskQuery query);
}
