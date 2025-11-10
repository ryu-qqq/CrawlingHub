package com.ryuqq.crawlinghub.application.product.port.out;

import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.SyncStatus;

import java.util.List;
import java.util.Optional;

/**
 * ProductSyncOutbox 조회 Port (Outbound)
 *
 * <p>Query Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface LoadProductSyncOutboxPort {

    /**
     * Outbox ID로 단건 조회
     *
     * @param outboxId Outbox ID
     * @return ProductSyncOutbox (없으면 Optional.empty())
     */
    Optional<ProductSyncOutbox> findById(Long outboxId);

    /**
     * 상태로 ProductSyncOutbox 목록 조회
     *
     * @param status SyncStatus (null 불가)
     * @param limit 최대 조회 개수
     * @return ProductSyncOutbox 목록
     * @throws IllegalArgumentException status가 null인 경우
     */
    List<ProductSyncOutbox> findByStatus(SyncStatus status, int limit);
}

