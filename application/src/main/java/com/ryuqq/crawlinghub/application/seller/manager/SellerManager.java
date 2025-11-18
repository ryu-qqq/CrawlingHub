package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.seller.port.out.SellerPersistencePort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Manager
 * - Seller Persistence Port만 의존
 * - 트랜잭션 짧게 유지
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@Component
@Transactional
public class SellerManager {

    private final SellerPersistencePort persistencePort;

    public SellerManager(SellerPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * Seller 저장 (트랜잭션)
     *
     * @param seller Seller Aggregate
     * @return 저장된 Seller ID
     */
    public SellerId save(Seller seller) {
        return persistencePort.persist(seller);
    }
}
