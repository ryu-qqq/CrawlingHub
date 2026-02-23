package com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.mapper.CrawlSchedulerCompositeMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.repository.CrawlSchedulerCompositeQueryDslRepository;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerCompositionQueryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler Composite 조회 Adapter
 *
 * <p>CrawlSchedulerCompositionQueryPort 구현체. Repository에서 3개 쿼리를 실행하고 Mapper로 조합합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerCompositionQueryAdapter implements CrawlSchedulerCompositionQueryPort {

    private static final int RECENT_TASKS_LIMIT = 10;

    private final CrawlSchedulerCompositeQueryDslRepository compositeRepository;
    private final CrawlSchedulerCompositeMapper compositeMapper;

    public CrawlSchedulerCompositionQueryAdapter(
            CrawlSchedulerCompositeQueryDslRepository compositeRepository,
            CrawlSchedulerCompositeMapper compositeMapper) {
        this.compositeRepository = compositeRepository;
        this.compositeMapper = compositeMapper;
    }

    @Override
    public Optional<CrawlSchedulerDetailResult> findSchedulerDetailById(Long schedulerId) {
        return compositeRepository
                .fetchSchedulerWithSeller(schedulerId)
                .map(
                        compositeDto -> {
                            List<CrawlSchedulerTaskSummaryDto> tasks =
                                    compositeRepository.fetchRecentTasks(
                                            schedulerId, RECENT_TASKS_LIMIT);
                            List<CrawlSchedulerTaskStatisticsDto> stats =
                                    compositeRepository.fetchTaskStatistics(schedulerId);
                            return compositeMapper.toResult(compositeDto, tasks, stats);
                        });
    }
}
