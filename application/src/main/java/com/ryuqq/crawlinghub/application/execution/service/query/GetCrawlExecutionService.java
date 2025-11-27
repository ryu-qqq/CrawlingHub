package com.ryuqq.crawlinghub.application.execution.service.query;

import com.ryuqq.crawlinghub.application.execution.assembler.CrawlExecutionAssembler;
import com.ryuqq.crawlinghub.application.execution.dto.query.GetCrawlExecutionQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;
import com.ryuqq.crawlinghub.application.execution.port.in.query.GetCrawlExecutionUseCase;
import com.ryuqq.crawlinghub.application.execution.port.out.query.CrawlExecutionQueryPort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.exception.CrawlExecutionNotFoundException;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlExecution 단건 조회 Service
 *
 * <p>GetCrawlExecutionUseCase 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetCrawlExecutionService implements GetCrawlExecutionUseCase {

    private final CrawlExecutionQueryPort crawlExecutionQueryPort;
    private final CrawlExecutionAssembler assembler;

    public GetCrawlExecutionService(
            CrawlExecutionQueryPort crawlExecutionQueryPort, CrawlExecutionAssembler assembler) {
        this.crawlExecutionQueryPort = crawlExecutionQueryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public CrawlExecutionDetailResponse execute(GetCrawlExecutionQuery query) {
        // 1. CrawlExecution 조회 (Port)
        CrawlExecution execution =
                crawlExecutionQueryPort
                        .findById(CrawlExecutionId.of(query.crawlExecutionId()))
                        .orElseThrow(
                                () -> new CrawlExecutionNotFoundException(query.crawlExecutionId()));

        // 2. 응답 변환 (Assembler)
        return assembler.toDetailResponse(execution);
    }
}
