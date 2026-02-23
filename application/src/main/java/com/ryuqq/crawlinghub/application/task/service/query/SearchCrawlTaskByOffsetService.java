package com.ryuqq.crawlinghub.application.task.service.query;

import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.query.CrawlTaskSearchParams;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;
import com.ryuqq.crawlinghub.application.task.factory.query.CrawlTaskQueryFactory;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.query.SearchCrawlTaskByOffsetUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 오프셋 기반 다건 조회 Service
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchCrawlTaskByOffsetService implements SearchCrawlTaskByOffsetUseCase {

    private final CrawlTaskReadManager readManager;
    private final CrawlTaskQueryFactory queryFactory;
    private final CrawlTaskAssembler assembler;

    public SearchCrawlTaskByOffsetService(
            CrawlTaskReadManager readManager,
            CrawlTaskQueryFactory queryFactory,
            CrawlTaskAssembler assembler) {
        this.readManager = readManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
    }

    @Override
    public CrawlTaskPageResult execute(CrawlTaskSearchParams params) {
        CrawlTaskCriteria criteria = queryFactory.createCriteria(params);

        List<CrawlTask> crawlTasks = readManager.findByCriteria(criteria);
        long totalElements = readManager.countByCriteria(criteria);

        return assembler.toPageResult(crawlTasks, params.page(), params.size(), totalElements);
    }
}
