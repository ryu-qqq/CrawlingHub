package com.ryuqq.crawlinghub.application.schedule.service.query;

import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.query.CrawlSchedulerSearchParams;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerPageResult;
import com.ryuqq.crawlinghub.application.schedule.factory.query.CrawlSchedulerQueryFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlSchedulerByOffsetUseCase;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 크롤 스케줄러 오프셋 기반 다건 조회 Service
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchCrawlSchedulerByOffsetService implements SearchCrawlSchedulerByOffsetUseCase {

    private final CrawlSchedulerReadManager readManager;
    private final CrawlSchedulerQueryFactory queryFactory;
    private final CrawlSchedulerAssembler assembler;

    public SearchCrawlSchedulerByOffsetService(
            CrawlSchedulerReadManager readManager,
            CrawlSchedulerQueryFactory queryFactory,
            CrawlSchedulerAssembler assembler) {
        this.readManager = readManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
    }

    @Override
    public CrawlSchedulerPageResult execute(CrawlSchedulerSearchParams params) {
        CrawlSchedulerSearchCriteria criteria = queryFactory.createCriteria(params);

        List<CrawlScheduler> schedulers = readManager.findByCriteria(criteria);
        long totalElements = readManager.countByCriteria(criteria);

        return assembler.toPageResult(schedulers, criteria.page(), criteria.size(), totalElements);
    }
}
