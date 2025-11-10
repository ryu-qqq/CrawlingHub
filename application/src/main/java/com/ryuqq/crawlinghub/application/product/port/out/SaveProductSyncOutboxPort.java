package com.ryuqq.crawlinghub.application.product.port.out;

import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;

/**
 * ProductSyncOutbox 저장 Port (Outbound)
 *
 * <p>Persistence Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface SaveProductSyncOutboxPort {

    /**
     * ProductSyncOutbox 저장 (신규 생성 또는 수정)
     *
     * @param outbox 저장할 ProductSyncOutbox (null 불가)
     * @return 저장된 ProductSyncOutbox (ID 포함)
     * @throws IllegalArgumentException outbox가 null인 경우
     */
    ProductSyncOutbox save(ProductSyncOutbox outbox);
}

