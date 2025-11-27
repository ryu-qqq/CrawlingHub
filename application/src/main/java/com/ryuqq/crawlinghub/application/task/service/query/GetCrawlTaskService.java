package com.ryuqq.crawlinghub.application.task.service.query;

import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.query.GetCrawlTaskQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.port.in.query.GetCrawlTaskUseCase;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlTask 단건 조회 Service
 *
 * <p>GetCrawlTaskUseCase 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetCrawlTaskService implements GetCrawlTaskUseCase {

    private final CrawlTaskQueryPort crawlTaskQueryPort;
    private final CrawlTaskAssembler assembler;

    public GetCrawlTaskService(
            CrawlTaskQueryPort crawlTaskQueryPort, CrawlTaskAssembler assembler) {
        this.crawlTaskQueryPort = crawlTaskQueryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public CrawlTaskDetailResponse execute(GetCrawlTaskQuery query) {
        // 1. CrawlTask 조회
        CrawlTask crawlTask =
                crawlTaskQueryPort
                        .findById(CrawlTaskId.of(query.crawlTaskId()))
                        .orElseThrow(() -> new CrawlTaskNotFoundException(query.crawlTaskId()));

        // 2. 응답 변환 (Assembler)
        return assembler.toDetailResponse(crawlTask);
    }
}
