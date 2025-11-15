package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSyncOutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ProductSyncOutbox JPA Repository
 *
 * <p><strong>CQRS 패턴 적용</strong></p>
 * <ul>
 *   <li>✅ Command: save(), delete()</li>
 *   <li>✅ Query: findById(), findByStatus() (간단한 조회만 허용)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-11
 */
public interface ProductSyncOutboxJpaRepository extends JpaRepository<ProductSyncOutboxEntity, Long> {

    /**
     * 상태로 조회 (생성일 오름차순)
     *
     * @param status 상태
     * @param pageable 페이징 정보
     * @return Outbox 목록
     */
    List<ProductSyncOutboxEntity> findByStatusOrderByCreatedAtAsc(
        ProductSyncOutboxEntity.SyncStatus status,
        Pageable pageable
    );
}

