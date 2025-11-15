package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSyncOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.ProductSyncOutboxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.ProductSyncOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.LoadProductSyncOutboxPort;
import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.SyncStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ProductSyncOutbox Query Adapter - CQRS Query Adapter (읽기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전용 (findById, findByStatus)</li>
 *   <li>✅ Entity → Domain 변환 (Mapper 사용)</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ ProductSyncOutbox Domain 반환</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Write 작업은 ProductSyncOutboxCommandAdapter에서 처리</li>
 *   <li>❌ Command 작업은 이 Adapter에서 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSyncOutboxQueryAdapter implements LoadProductSyncOutboxPort {

    private final ProductSyncOutboxJpaRepository jpaRepository;
    private final ProductSyncOutboxMapper mapper;

    /**
     * Adapter 생성자
     *
     * @param jpaRepository JPA Repository
     * @param mapper Entity → Domain Mapper
     */
    public ProductSyncOutboxQueryAdapter(
        ProductSyncOutboxJpaRepository jpaRepository,
        ProductSyncOutboxMapper mapper
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    /**
     * ID로 ProductSyncOutbox 조회
     *
     * <p>Entity 조회 후 Domain으로 변환합니다.</p>
     *
     * @param outboxId Outbox ID (null 불가)
     * @return ProductSyncOutbox Domain (없으면 Optional.empty())
     * @throws IllegalArgumentException outboxId가 null인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSyncOutbox> findById(Long outboxId) {
        Objects.requireNonNull(outboxId, "outboxId must not be null");

        return jpaRepository.findById(outboxId)
            .map(mapper::toDomain);
    }

    /**
     * 상태로 ProductSyncOutbox 목록 조회
     *
     * <p>Entity 조회 후 Domain으로 변환합니다.</p>
     *
     * @param status SyncStatus (null 불가)
     * @param limit 최대 조회 개수
     * @return ProductSyncOutbox Domain 목록
     * @throws IllegalArgumentException status가 null인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductSyncOutbox> findByStatus(SyncStatus status, int limit) {
        Objects.requireNonNull(status, "status must not be null");

        ProductSyncOutboxEntity.SyncStatus entityStatus = toEntityStatus(status);
        return jpaRepository.findByStatusOrderByCreatedAtAsc(entityStatus, PageRequest.of(0, limit))
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    /**
     * Domain SyncStatus를 Entity SyncStatus로 변환
     *
     * @param domainStatus Domain SyncStatus
     * @return Entity SyncStatus
     */
    private ProductSyncOutboxEntity.SyncStatus toEntityStatus(SyncStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> ProductSyncOutboxEntity.SyncStatus.PENDING;
            case PROCESSING -> ProductSyncOutboxEntity.SyncStatus.PROCESSING;
            case COMPLETED -> ProductSyncOutboxEntity.SyncStatus.COMPLETED;
            case FAILED -> ProductSyncOutboxEntity.SyncStatus.FAILED;
        };
    }
}

