package com.ryuqq.crawlinghub.adapter.out.persistence.execution.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity.CrawlExecutionJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.mapper.CrawlExecutionJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.repository.CrawlExecutionJpaRepository;
import com.ryuqq.crawlinghub.application.execution.port.out.command.CrawlExecutionPersistencePort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.id.CrawlExecutionId;
import org.springframework.stereotype.Component;

/**
 * CrawlExecutionCommandAdapter - CrawlExecution Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain Aggregate → JPA Entity 변환
 *   <li>JpaRepository.save() 호출
 *   <li>CrawlExecutionId 반환
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
public class CrawlExecutionCommandAdapter implements CrawlExecutionPersistencePort {

    private final CrawlExecutionJpaRepository jpaRepository;
    private final CrawlExecutionJpaEntityMapper mapper;

    public CrawlExecutionCommandAdapter(
            CrawlExecutionJpaRepository jpaRepository, CrawlExecutionJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * CrawlExecution 저장 (신규 생성 또는 수정)
     *
     * <p>ID가 미할당이면 INSERT, ID가 할당되어 있으면 JPA dirty checking을 통해 UPDATE됩니다.
     *
     * @param crawlExecution 저장할 CrawlExecution Aggregate
     * @return 저장된 CrawlExecution의 ID
     */
    @Override
    public CrawlExecutionId persist(CrawlExecution crawlExecution) {
        // 1. Domain → Entity 변환
        CrawlExecutionJpaEntity entity = mapper.toEntity(crawlExecution);

        // 2. JPA 저장
        CrawlExecutionJpaEntity savedEntity = jpaRepository.save(entity);

        // 3. ID 반환
        return CrawlExecutionId.of(savedEntity.getId());
    }
}
