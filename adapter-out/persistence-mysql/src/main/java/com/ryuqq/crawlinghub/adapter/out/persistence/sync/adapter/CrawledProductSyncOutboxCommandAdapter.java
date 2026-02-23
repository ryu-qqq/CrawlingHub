package com.ryuqq.crawlinghub.adapter.out.persistence.sync.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.sync.entity.ProductSyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.mapper.ProductSyncOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.repository.ProductSyncOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductSyncOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.springframework.stereotype.Component;

/**
 * CrawledProductSyncOutboxCommandAdapter - CrawledProductSyncOutbox Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain Aggregate -> JPA Entity 변환
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
public class CrawledProductSyncOutboxCommandAdapter
        implements CrawledProductSyncOutboxPersistencePort {

    private final ProductSyncOutboxJpaRepository jpaRepository;
    private final ProductSyncOutboxJpaEntityMapper mapper;

    public CrawledProductSyncOutboxCommandAdapter(
            ProductSyncOutboxJpaRepository jpaRepository, ProductSyncOutboxJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * CrawledProductSyncOutbox 저장 (신규 생성)
     *
     * @param outbox 저장할 CrawledProductSyncOutbox
     */
    @Override
    public void persist(CrawledProductSyncOutbox outbox) {
        ProductSyncOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }

    /**
     * CrawledProductSyncOutbox 상태 업데이트
     *
     * <p>ID가 있는 Entity를 저장하면 JPA가 UPDATE를 수행합니다.
     *
     * @param outbox 업데이트할 CrawledProductSyncOutbox
     */
    @Override
    public void update(CrawledProductSyncOutbox outbox) {
        ProductSyncOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }
}
