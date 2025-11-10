package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSyncOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ProductSyncOutbox JPA Repository
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Spring Data JPA 사용</li>
 *   <li>✅ Query 메서드 네이밍 규칙 준수</li>
 *   <li>✅ List 반환 (findByStatus)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface ProductSyncOutboxJpaRepository extends JpaRepository<ProductSyncOutboxEntity, Long> {

    /**
     * 상태로 ProductSyncOutbox 목록 조회 (생성 시간 순)
     *
     * @param status 동기화 상태
     * @param limit 최대 조회 개수
     * @return ProductSyncOutbox 목록
     */
    @Query("SELECT o FROM ProductSyncOutboxEntity o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<ProductSyncOutboxEntity> findByStatusOrderByCreatedAtAsc(@Param("status") ProductSyncOutboxEntity.SyncStatus status);
}

