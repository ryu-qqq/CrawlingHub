package com.ryuqq.crawlinghub.application.seller.port.out;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * Seller Persistence Port (Command)
 *
 * <p>Seller Aggregate를 영속화하는 쓰기 전용 Port</p>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public interface SellerPersistencePort {

    /**
     * Seller 저장 (신규 생성 또는 수정)
     *
     * <p>신규 생성 (ID 없음) → INSERT</p>
     * <p>기존 수정 (ID 있음) → UPDATE (JPA 더티체킹)</p>
     *
     * @param seller 저장할 Seller (Domain Aggregate)
     * @return 저장된 Seller의 ID (Value Object)
     */
    SellerId persist(Seller seller);
}
