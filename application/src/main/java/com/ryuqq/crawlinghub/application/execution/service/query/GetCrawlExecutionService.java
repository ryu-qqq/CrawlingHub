package com.ryuqq.crawlinghub.application.execution.service.query;

import com.ryuqq.crawlinghub.application.execution.assembler.CrawlExecutionAssembler;
import com.ryuqq.crawlinghub.application.execution.dto.query.GetCrawlExecutionQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;
import com.ryuqq.crawlinghub.application.execution.manager.query.CrawlExecutionReadManager;
import com.ryuqq.crawlinghub.application.execution.port.in.query.GetCrawlExecutionUseCase;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.exception.CrawlExecutionNotFoundException;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import org.springframework.stereotype.Service;

/**
 * CrawlExecution 단건 조회 Service
 *
 * <p>GetCrawlExecutionUseCase 구현체
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetCrawlExecutionService implements GetCrawlExecutionUseCase {

    private final CrawlExecutionReadManager readManager;
    private final CrawlExecutionAssembler assembler;

    public GetCrawlExecutionService(
            CrawlExecutionReadManager readManager, CrawlExecutionAssembler assembler) {
        this.readManager = readManager;
        this.assembler = assembler;
    }

    @Override
    public CrawlExecutionDetailResponse execute(GetCrawlExecutionQuery query) {
        // 1. CrawlExecution 조회 (ReadManager)
        CrawlExecution execution =
                readManager
                        .findById(CrawlExecutionId.of(query.crawlExecutionId()))
                        .orElseThrow(
                                () ->
                                        new CrawlExecutionNotFoundException(
                                                query.crawlExecutionId()));

        // 2. 응답 변환 (Assembler)
        return assembler.toDetailResponse(execution);
    }
}
