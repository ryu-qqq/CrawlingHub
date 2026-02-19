package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerJpaRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.command.PersistCrawlSchedulePort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerCommandAdapter - CrawlScheduler Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain Aggregate → JPA Entity 변환
 *   <li>JpaRepository.save() 호출
 *   <li>CrawlSchedulerId 반환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직 (Domain에서 처리)
 *   <li>❌ 조회 로직 (QueryAdapter로 분리)
 *   <li>❌ @Transactional 어노테이션 (Application Layer에서 관리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerCommandAdapter implements PersistCrawlSchedulePort {

    private final CrawlSchedulerJpaRepository jpaRepository;
    private final CrawlSchedulerJpaEntityMapper mapper;

    public CrawlSchedulerCommandAdapter(
            CrawlSchedulerJpaRepository jpaRepository, CrawlSchedulerJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * CrawlScheduler 저장 (신규 생성 또는 수정)
     *
     * @param crawlScheduler 저장할 CrawlScheduler Aggregate
     * @return 저장된 CrawlScheduler의 ID
     */
    @Override
    public CrawlSchedulerId persist(CrawlScheduler crawlScheduler) {
        // 1. Domain → Entity 변환
        CrawlSchedulerJpaEntity entity = mapper.toEntity(crawlScheduler);

        // 2. JPA 저장
        CrawlSchedulerJpaEntity savedEntity = jpaRepository.save(entity);

        // 3. ID 반환
        return CrawlSchedulerId.of(savedEntity.getId());
    }
}
