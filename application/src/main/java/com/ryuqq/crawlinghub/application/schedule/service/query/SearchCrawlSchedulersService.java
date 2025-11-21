package com.ryuqq.crawlinghub.application.schedule.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlSchedulesUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Search CrawlSchedulers Service
 *
 * <p>크롤 스케줄러 목록 조회 UseCase 구현
 *
 * <ul>
 *   <li>조회 전용 (읽기 전용 트랜잭션)
 *   <li>Query DTO → Criteria 변환
 *   <li>Domain → Response 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchCrawlSchedulersService implements SearchCrawlSchedulesUseCase {

    private final CrawlScheduleQueryPort crawlScheduleQueryPort;
    private final CrawlSchedulerAssembler assembler;

    public SearchCrawlSchedulersService(
            CrawlScheduleQueryPort crawlScheduleQueryPort, CrawlSchedulerAssembler assembler) {
        this.crawlScheduleQueryPort = crawlScheduleQueryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrawlSchedulerResponse> execute(SearchCrawlSchedulersQuery query) {
        // 1. Query → Criteria 변환 (Assembler)
        CrawlSchedulerQueryCriteria criteria = assembler.toCriteria(query);

        // 2. 조회
        List<CrawlScheduler> schedulers = crawlScheduleQueryPort.findByCriteria(criteria);
        long totalElements = crawlScheduleQueryPort.count(criteria);

        // 3. Domain → PageResponse 변환 (Assembler)
        return assembler.toPageResponse(
                schedulers, criteria.page(), criteria.size(), totalElements);
    }
}
