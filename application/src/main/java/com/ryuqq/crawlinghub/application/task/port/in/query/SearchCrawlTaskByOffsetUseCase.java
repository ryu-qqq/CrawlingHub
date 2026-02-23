package com.ryuqq.crawlinghub.application.task.port.in.query;

import com.ryuqq.crawlinghub.application.task.dto.query.CrawlTaskSearchParams;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;

/**
 * CrawlTask 오프셋 기반 다건 조회 UseCase (Port In)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchCrawlTaskByOffsetUseCase {

    /**
     * CrawlTask 다건 조회
     *
     * @param params 검색 파라미터
     * @return 태스크 페이지 결과
     */
    CrawlTaskPageResult execute(CrawlTaskSearchParams params);
}
