package com.ryuqq.crawlinghub.application.execution.port.in.query;

import com.ryuqq.crawlinghub.application.execution.dto.query.GetCrawlExecutionQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;

/**
 * CrawlExecution 단건 조회 UseCase (Port In - Query)
 *
 * <p>CrawlExecution 상세 정보 조회 (결과 데이터 포함)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetCrawlExecutionUseCase {

    /**
     * CrawlExecution 상세 조회 실행
     *
     * @param query 조회 쿼리 (crawlExecutionId)
     * @return CrawlExecution 상세 응답
     * @throws com.ryuqq.crawlinghub.domain.execution.exception.CrawlExecutionNotFoundException 해당
     *     ID의 CrawlExecution이 없는 경우
     */
    CrawlExecutionDetailResponse execute(GetCrawlExecutionQuery query);
}
