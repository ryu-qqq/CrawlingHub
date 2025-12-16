package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.SyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.SyncOutboxJpaEntity.OutboxStatus;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.SyncOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.SyncOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.application.product.port.out.query.SyncOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * SyncOutboxQueryAdapter - SyncOutbox Query Adapter
 *
 * <p>CQRS의 Query(조회) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>QueryDslRepository 호출
 *   <li>JPA Entity → Domain 변환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>비즈니스 로직 (Domain에서 처리)
 *   <li>쓰기 로직 (CommandAdapter로 분리)
 *   <li>@Transactional 어노테이션 (Application Layer에서 관리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SyncOutboxQueryAdapter implements SyncOutboxQueryPort {

    private final SyncOutboxQueryDslRepository queryDslRepository;
    private final SyncOutboxJpaEntityMapper mapper;

    public SyncOutboxQueryAdapter(
            SyncOutboxQueryDslRepository queryDslRepository, SyncOutboxJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CrawledProductSyncOutbox> findById(Long outboxId) {
        return queryDslRepository.findById(outboxId).map(mapper::toDomain);
    }

    @Override
    public Optional<CrawledProductSyncOutbox> findByIdempotencyKey(String idempotencyKey) {
        return queryDslRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public List<CrawledProductSyncOutbox> findByCrawledProductId(
            CrawledProductId crawledProductId) {
        List<SyncOutboxJpaEntity> entities =
                queryDslRepository.findByCrawledProductId(crawledProductId.value());
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CrawledProductSyncOutbox> findByStatus(ProductOutboxStatus status, int limit) {
        OutboxStatus entityStatus = toEntityStatus(status);
        List<SyncOutboxJpaEntity> entities = queryDslRepository.findByStatus(entityStatus, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CrawledProductSyncOutbox> findPendingOutboxes(int limit) {
        List<SyncOutboxJpaEntity> entities = queryDslRepository.findPendingOutboxes(limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CrawledProductSyncOutbox> findRetryableOutboxes(int maxRetryCount, int limit) {
        List<SyncOutboxJpaEntity> entities =
                queryDslRepository.findRetryableOutboxes(maxRetryCount, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    private OutboxStatus toEntityStatus(ProductOutboxStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> OutboxStatus.PENDING;
            case PROCESSING -> OutboxStatus.PROCESSING;
            case COMPLETED -> OutboxStatus.COMPLETED;
            case FAILED -> OutboxStatus.FAILED;
        };
    }
}
