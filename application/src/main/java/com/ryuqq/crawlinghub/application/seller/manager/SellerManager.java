package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.seller.port.out.SellerPersistencePort;
import com.ryuqq.crawlinghub.application.seller.port.out.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Manager
 * - Seller Persistence Port 의존
 * - 트랜잭션 짧게 유지
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@Component
@Transactional
public class SellerManager {

    private final SellerPersistencePort persistencePort;
    private final SellerQueryPort queryPort;

    public SellerManager(
            SellerPersistencePort persistencePort,
            SellerQueryPort queryPort
    ) {
        this.persistencePort = persistencePort;
        this.queryPort = queryPort;
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

    /**
     * Seller 조회 후 수정 (트랜잭션)
     *
     * @param sellerId Seller ID
     * @param newName 새로운 이름
     */
    public void updateName(Long sellerId, String newName) {
        Seller seller = queryPort.findBySellerId(SellerId.of(sellerId))
                .orElseThrow(() -> new SellerNotFoundException("Seller를 찾을 수 없습니다: " + sellerId));

        seller.updateName(newName);
        persistencePort.persist(seller);
    }
}
