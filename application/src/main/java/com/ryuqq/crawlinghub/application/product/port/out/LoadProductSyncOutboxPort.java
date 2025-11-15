package com.ryuqq.crawlinghub.application.product.port.out;

import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.SyncStatus;

import java.util.List;
import java.util.Optional;

/**
 * ProductSyncOutbox 조회 Port (Outbound) - CQRS Query Port
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Query 전용 Port (읽기 전용)</li>
 *   <li>✅ ProductSyncOutbox Domain 반환</li>
 *   <li>✅ Adapter에서 Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>구현체:</strong></p>
 * <ul>
 *   <li>ProductSyncOutboxQueryAdapter (Query Adapter)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface LoadProductSyncOutboxPort {

    /**
     * Outbox ID로 단건 조회
     *
     * @param outboxId Outbox ID
     * @return ProductSyncOutbox Domain (없으면 Optional.empty())
     */
    Optional<ProductSyncOutbox> findById(Long outboxId);

    /**
     * 상태로 ProductSyncOutbox 목록 조회
     *
     * @param status SyncStatus (null 불가)
     * @param limit 최대 조회 개수
     * @return ProductSyncOutbox Domain 목록
     * @throws IllegalArgumentException status가 null인 경우
     */
    List<ProductSyncOutbox> findByStatus(SyncStatus status, int limit);
}

