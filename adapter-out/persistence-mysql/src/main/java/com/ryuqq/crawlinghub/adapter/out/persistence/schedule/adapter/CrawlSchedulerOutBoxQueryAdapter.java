package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerOutBoxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerOutBoxQueryDslRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerOutBoxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerOutBoxQueryAdapter - CrawlSchedulerOutBox Query Adapter
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerOutBoxQueryAdapter implements CrawlSchedulerOutBoxQueryPort {

    private final CrawlSchedulerOutBoxQueryDslRepository queryDslRepository;
    private final CrawlSchedulerOutBoxJpaEntityMapper mapper;

    public CrawlSchedulerOutBoxQueryAdapter(
            CrawlSchedulerOutBoxQueryDslRepository queryDslRepository,
            CrawlSchedulerOutBoxJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CrawlSchedulerOutBox> findByHistoryId(CrawlSchedulerHistoryId historyId) {
        return queryDslRepository.findByHistoryId(historyId.value()).map(mapper::toDomain);
    }

    @Override
    public List<CrawlSchedulerOutBox> findByStatus(CrawlSchedulerOubBoxStatus status, int limit) {
        List<CrawlSchedulerOutBoxJpaEntity> entities =
                queryDslRepository.findByStatus(status, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CrawlSchedulerOutBox> findPendingOlderThan(int limit, int delaySeconds) {
        List<CrawlSchedulerOutBoxJpaEntity> entities =
                queryDslRepository.findPendingOlderThan(limit, delaySeconds);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CrawlSchedulerOutBox> findStaleProcessing(int limit, long timeoutSeconds) {
        List<CrawlSchedulerOutBoxJpaEntity> entities =
                queryDslRepository.findStaleProcessing(limit, timeoutSeconds);
        return entities.stream().map(mapper::toDomain).toList();
    }
}
