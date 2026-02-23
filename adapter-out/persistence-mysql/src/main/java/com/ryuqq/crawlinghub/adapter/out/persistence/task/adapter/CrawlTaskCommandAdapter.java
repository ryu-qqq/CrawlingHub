package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskJpaRepository;
import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskPersistencePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskCommandAdapter - CrawlTask Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain Aggregate → JPA Entity 변환
 *   <li>JpaRepository.save() 호출
 *   <li>CrawlTaskId 반환
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
public class CrawlTaskCommandAdapter implements CrawlTaskPersistencePort {

    private final CrawlTaskJpaRepository jpaRepository;
    private final CrawlTaskJpaEntityMapper mapper;

    public CrawlTaskCommandAdapter(
            CrawlTaskJpaRepository jpaRepository, CrawlTaskJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * CrawlTask 저장 (신규 생성 또는 수정)
     *
     * <p>ID가 미할당이면 INSERT, ID가 할당되어 있으면 JPA dirty checking을 통해 UPDATE됩니다.
     *
     * @param crawlTask 저장할 CrawlTask Aggregate
     * @return 저장된 CrawlTask의 ID
     */
    @Override
    public CrawlTaskId persist(CrawlTask crawlTask) {
        // 1. Domain → Entity 변환
        CrawlTaskJpaEntity entity = mapper.toEntity(crawlTask);

        // 2. JPA 저장
        CrawlTaskJpaEntity savedEntity = jpaRepository.save(entity);

        // 3. ID 반환
        return CrawlTaskId.of(savedEntity.getId());
    }
}
