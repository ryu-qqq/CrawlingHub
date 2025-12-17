package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.SyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.SyncOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.SyncOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.command.SyncOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.springframework.stereotype.Component;

/**
 * SyncOutboxCommandAdapter - SyncOutbox Command Adapter
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
 *   <li>비즈니스 로직 (Domain에서 처리)
 *   <li>조회 로직 (QueryAdapter로 분리)
 *   <li>@Transactional 어노테이션 (Application Layer에서 관리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SyncOutboxCommandAdapter implements SyncOutboxPersistencePort {

    private final SyncOutboxJpaRepository jpaRepository;
    private final SyncOutboxJpaEntityMapper mapper;

    public SyncOutboxCommandAdapter(
            SyncOutboxJpaRepository jpaRepository, SyncOutboxJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * SyncOutbox 저장 (신규 생성)
     *
     * @param outbox 저장할 Outbox
     */
    @Override
    public void persist(CrawledProductSyncOutbox outbox) {
        SyncOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }

    /**
     * SyncOutbox 상태 업데이트
     *
     * @param outbox 업데이트할 Outbox
     */
    @Override
    public void update(CrawledProductSyncOutbox outbox) {
        SyncOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }
}
