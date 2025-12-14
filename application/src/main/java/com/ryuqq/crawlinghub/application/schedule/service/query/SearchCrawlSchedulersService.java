package com.ryuqq.crawlinghub.application.schedule.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.factory.query.CrawlSchedulerQueryFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlSchedulesUseCase;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Search CrawlSchedulers Service
 *
 * <p>크롤 스케줄러 목록 조회 UseCase 구현
 *
 * <ul>
 *   <li>조회 전용
 *   <li>Query DTO → Criteria 변환 (QueryFactory)
 *   <li>Domain → Response 변환 (Assembler)
 * </ul>
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchCrawlSchedulersService implements SearchCrawlSchedulesUseCase {

    private final CrawlSchedulerReadManager readManager;
    private final CrawlSchedulerQueryFactory queryFactory;
    private final CrawlSchedulerAssembler assembler;

    public SearchCrawlSchedulersService(
            CrawlSchedulerReadManager readManager,
            CrawlSchedulerQueryFactory queryFactory,
            CrawlSchedulerAssembler assembler) {
        this.readManager = readManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
    }

    @Override
    public PageResponse<CrawlSchedulerResponse> execute(SearchCrawlSchedulersQuery query) {
        // 1. Query → Criteria 변환 (QueryFactory)
        CrawlSchedulerQueryCriteria criteria = queryFactory.createCriteria(query);

        // 2. 조회
        List<CrawlScheduler> schedulers = readManager.findByCriteria(criteria);
        long totalElements = readManager.count(criteria);

        // 3. Domain → PageResponse 변환 (Assembler)
        return assembler.toPageResponse(
                schedulers, criteria.page(), criteria.size(), totalElements);
    }
}
