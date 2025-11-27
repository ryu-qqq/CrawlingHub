package com.ryuqq.crawlinghub.application.execution.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.assembler.CrawlExecutionAssembler;
import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;
import com.ryuqq.crawlinghub.application.execution.port.in.query.ListCrawlExecutionsUseCase;
import com.ryuqq.crawlinghub.application.execution.port.out.query.CrawlExecutionQueryPort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlExecution 목록 조회 Service
 *
 * <p>ListCrawlExecutionsUseCase 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ListCrawlExecutionsService implements ListCrawlExecutionsUseCase {

    private final CrawlExecutionQueryPort crawlExecutionQueryPort;
    private final CrawlExecutionAssembler assembler;

    public ListCrawlExecutionsService(
            CrawlExecutionQueryPort crawlExecutionQueryPort, CrawlExecutionAssembler assembler) {
        this.crawlExecutionQueryPort = crawlExecutionQueryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrawlExecutionResponse> execute(ListCrawlExecutionsQuery query) {
        // 1. Query → Criteria 변환 (Assembler)
        CrawlExecutionCriteria criteria = assembler.toCriteria(query);

        // 2. 목록 조회 (Port)
        List<CrawlExecution> executions = crawlExecutionQueryPort.findByCriteria(criteria);

        // 3. 총 개수 조회 (Port)
        long totalElements = crawlExecutionQueryPort.countByCriteria(criteria);

        // 4. PageResponse 변환 (Assembler)
        return assembler.toPageResponse(executions, query.page(), query.size(), totalElements);
    }
}
