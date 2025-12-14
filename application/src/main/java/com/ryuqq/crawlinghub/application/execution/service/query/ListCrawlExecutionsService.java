package com.ryuqq.crawlinghub.application.execution.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.assembler.CrawlExecutionAssembler;
import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;
import com.ryuqq.crawlinghub.application.execution.factory.query.CrawlExecutionQueryFactory;
import com.ryuqq.crawlinghub.application.execution.manager.query.CrawlExecutionReadManager;
import com.ryuqq.crawlinghub.application.execution.port.in.query.ListCrawlExecutionsUseCase;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * CrawlExecution 목록 조회 Service
 *
 * <p>ListCrawlExecutionsUseCase 구현체
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ListCrawlExecutionsService implements ListCrawlExecutionsUseCase {

    private final CrawlExecutionReadManager readManager;
    private final CrawlExecutionQueryFactory queryFactory;
    private final CrawlExecutionAssembler assembler;

    public ListCrawlExecutionsService(
            CrawlExecutionReadManager readManager,
            CrawlExecutionQueryFactory queryFactory,
            CrawlExecutionAssembler assembler) {
        this.readManager = readManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
    }

    @Override
    public PageResponse<CrawlExecutionResponse> execute(ListCrawlExecutionsQuery query) {
        // 1. Query → Criteria 변환 (QueryFactory)
        CrawlExecutionCriteria criteria = queryFactory.createCriteria(query);

        // 2. 목록 조회 (ReadManager)
        List<CrawlExecution> executions = readManager.findByCriteria(criteria);

        // 3. 총 개수 조회 (ReadManager)
        long totalElements = readManager.countByCriteria(criteria);

        // 4. PageResponse 변환 (Assembler)
        return assembler.toPageResponse(executions, query.page(), query.size(), totalElements);
    }
}
