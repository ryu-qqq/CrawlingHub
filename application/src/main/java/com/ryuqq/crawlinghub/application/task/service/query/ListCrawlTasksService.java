package com.ryuqq.crawlinghub.application.task.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.factory.query.CrawlTaskQueryFactory;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.query.ListCrawlTasksUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 목록 조회 Service
 *
 * <p>ListCrawlTasksUseCase 구현체
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ListCrawlTasksService implements ListCrawlTasksUseCase {

    private final CrawlTaskReadManager readManager;
    private final CrawlTaskQueryFactory queryFactory;
    private final CrawlTaskAssembler assembler;

    public ListCrawlTasksService(
            CrawlTaskReadManager readManager,
            CrawlTaskQueryFactory queryFactory,
            CrawlTaskAssembler assembler) {
        this.readManager = readManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
    }

    @Override
    public PageResponse<CrawlTaskResponse> execute(ListCrawlTasksQuery query) {
        // 1. Query → Criteria 변환 (QueryFactory)
        CrawlTaskCriteria criteria = queryFactory.createCriteria(query);

        // 2. CrawlTask 목록 조회
        List<CrawlTask> crawlTasks = readManager.findByCriteria(criteria);

        // 3. 총 개수 조회
        long totalElements = readManager.countByCriteria(criteria);

        // 4. PageResponse로 변환 (Assembler)
        return assembler.toPageResponse(crawlTasks, query.page(), query.size(), totalElements);
    }
}
