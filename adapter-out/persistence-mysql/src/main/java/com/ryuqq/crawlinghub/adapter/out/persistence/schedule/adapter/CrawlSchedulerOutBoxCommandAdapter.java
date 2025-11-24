package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerOutBoxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerOutBoxJpaRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.command.PersistCrawlScheduleOutBoxPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOutBoxId;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerOutBoxCommandAdapter - CrawlSchedulerOutBox Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerOutBoxCommandAdapter implements PersistCrawlScheduleOutBoxPort {

    private final CrawlSchedulerOutBoxJpaRepository jpaRepository;
    private final CrawlSchedulerOutBoxJpaEntityMapper mapper;

    public CrawlSchedulerOutBoxCommandAdapter(
            CrawlSchedulerOutBoxJpaRepository jpaRepository,
            CrawlSchedulerOutBoxJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * CrawlSchedulerOutBox 저장
     *
     * @param crawlSchedulerOutBox 저장할 CrawlSchedulerOutBox Aggregate
     * @return 저장된 CrawlSchedulerOutBox의 ID
     */
    @Override
    public CrawlSchedulerOutBoxId persist(CrawlSchedulerOutBox crawlSchedulerOutBox) {
        // 1. Domain → Entity 변환
        CrawlSchedulerOutBoxJpaEntity entity = mapper.toEntity(crawlSchedulerOutBox);

        // 2. JPA 저장
        CrawlSchedulerOutBoxJpaEntity savedEntity = jpaRepository.save(entity);

        // 3. ID 반환
        return CrawlSchedulerOutBoxId.of(savedEntity.getId());
    }
}
