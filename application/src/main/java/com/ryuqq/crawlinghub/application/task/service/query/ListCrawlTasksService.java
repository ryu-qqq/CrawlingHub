package com.ryuqq.crawlinghub.application.task.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.port.in.query.ListCrawlTasksUseCase;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            CrawlTaskQueryPort crawlTaskQueryPort, CrawlTaskAssembler assembler) {
        this.crawlTaskQueryPort = crawlTaskQueryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrawlTaskResponse> execute(ListCrawlTasksQuery query) {
        // 1. Query → Criteria 변환 (Assembler)
        CrawlTaskCriteria criteria = assembler.toCriteria(query);

        // 2. CrawlTask 목록 조회
        List<CrawlTask> crawlTasks = crawlTaskQueryPort.findByCriteria(criteria);

        // 3. 총 개수 조회
        long totalElements = crawlTaskQueryPort.countByCriteria(criteria);

        // 4. PageResponse로 변환 (Assembler)
        return assembler.toPageResponse(crawlTasks, query.page(), query.size(), totalElements);
    }
}
