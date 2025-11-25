package com.ryuqq.crawlinghub.application.crawl.task.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.crawl.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.crawl.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.crawl.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.crawl.task.port.in.query.ListCrawlTasksUseCase;
import com.ryuqq.crawlinghub.application.crawl.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.crawl.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CrawlTask 목록 조회 Service
 *
 * <p>ListCrawlTasksUseCase 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ListCrawlTasksService implements ListCrawlTasksUseCase {

    private final CrawlTaskQueryPort crawlTaskQueryPort;
    private final CrawlTaskAssembler assembler;

    public ListCrawlTasksService(
            CrawlTaskQueryPort crawlTaskQueryPort,
            CrawlTaskAssembler assembler
    ) {
        this.crawlTaskQueryPort = crawlTaskQueryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrawlTaskResponse> execute(ListCrawlTasksQuery query) {
        CrawlSchedulerId crawlSchedulerId = CrawlSchedulerId.of(query.crawlSchedulerId());

        // 1. CrawlTask 목록 조회
        List<CrawlTask> crawlTasks = crawlTaskQueryPort.findByScheduleId(
                crawlSchedulerId,
                query.status(),
                query.offset(),
                query.size()
        );

        // 2. 총 개수 조회
        long totalElements = crawlTaskQueryPort.countByScheduleId(
                crawlSchedulerId,
                query.status()
        );

        // 3. PageResponse로 변환 (Assembler)
        return assembler.toPageResponse(crawlTasks, query.page(), query.size(), totalElements);
    }
}
