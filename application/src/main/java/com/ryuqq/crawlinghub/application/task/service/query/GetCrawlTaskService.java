package com.ryuqq.crawlinghub.application.task.service.query;

import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.query.GetCrawlTaskQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.query.GetCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 단건 조회 Service
 *
 * <p>GetCrawlTaskUseCase 구현체
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetCrawlTaskService implements GetCrawlTaskUseCase {

    private final CrawlTaskReadManager readManager;
    private final CrawlTaskAssembler assembler;

    public GetCrawlTaskService(CrawlTaskReadManager readManager, CrawlTaskAssembler assembler) {
        this.readManager = readManager;
        this.assembler = assembler;
    }

    @Override
    public CrawlTaskDetailResponse execute(GetCrawlTaskQuery query) {
        // 1. CrawlTask 조회
        CrawlTask crawlTask =
                readManager
                        .findById(CrawlTaskId.of(query.crawlTaskId()))
                        .orElseThrow(() -> new CrawlTaskNotFoundException(query.crawlTaskId()));

        // 2. 응답 변환 (Assembler)
        return assembler.toDetailResponse(crawlTask);
    }
}
