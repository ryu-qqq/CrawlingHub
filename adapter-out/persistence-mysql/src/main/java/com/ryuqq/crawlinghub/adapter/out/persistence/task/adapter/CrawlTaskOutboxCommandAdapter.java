package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskOutboxCommandAdapter - CrawlTaskOutbox Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain Aggregate → JPA Entity 변환
 *   <li>JpaRepository.save() 호출
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
public class CrawlTaskOutboxCommandAdapter implements CrawlTaskOutboxPersistencePort {

    private final CrawlTaskOutboxJpaRepository jpaRepository;
    private final CrawlTaskOutboxJpaEntityMapper mapper;

    public CrawlTaskOutboxCommandAdapter(
            CrawlTaskOutboxJpaRepository jpaRepository, CrawlTaskOutboxJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * CrawlTaskOutbox 저장 (신규 생성 또는 수정)
     *
     * <p>ID가 없으면 INSERT, ID가 있으면 JPA dirty checking을 통해 UPDATE됩니다.
     *
     * @param outbox 저장할 CrawlTaskOutbox
     */
    @Override
    public void persist(CrawlTaskOutbox outbox) {
        // 1. Domain → Entity 변환
        CrawlTaskOutboxJpaEntity entity = mapper.toEntity(outbox);

        // 2. JPA 저장
        jpaRepository.save(entity);
    }
}
