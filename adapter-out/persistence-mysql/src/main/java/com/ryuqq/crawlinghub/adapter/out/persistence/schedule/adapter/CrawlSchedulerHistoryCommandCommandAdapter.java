package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerHistoryJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerHistoryJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerHistoryJpaRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.command.CrawlScheduleHistoryCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerHistoryCommandAdapter - CrawlSchedulerHistory Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerHistoryCommandCommandAdapter implements CrawlScheduleHistoryCommandPort {

    private final CrawlSchedulerHistoryJpaRepository jpaRepository;
    private final CrawlSchedulerHistoryJpaEntityMapper mapper;

    public CrawlSchedulerHistoryCommandCommandAdapter(
            CrawlSchedulerHistoryJpaRepository jpaRepository,
            CrawlSchedulerHistoryJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * CrawlSchedulerHistory 저장
     *
     * @param crawlSchedulerHistory 저장할 CrawlSchedulerHistory Aggregate
     * @return 저장된 CrawlSchedulerHistory의 ID
     */
    @Override
    public CrawlSchedulerHistoryId persist(CrawlSchedulerHistory crawlSchedulerHistory) {
        // 1. Domain → Entity 변환
        CrawlSchedulerHistoryJpaEntity entity = mapper.toEntity(crawlSchedulerHistory);

        // 2. JPA 저장
        CrawlSchedulerHistoryJpaEntity savedEntity = jpaRepository.save(entity);

        // 3. ID 반환
        return CrawlSchedulerHistoryId.of(savedEntity.getId());
    }
}
