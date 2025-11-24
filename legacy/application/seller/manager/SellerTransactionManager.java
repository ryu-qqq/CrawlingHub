package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Transaction Manager - SellerPersistencePort만 의존 - 트랜잭션 짧게 유지
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Transactional
public class SellerTransactionManager {

    private final SellerPersistencePort persistencePort;

    public SellerTransactionManager(SellerPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * Seller 저장 (트랜잭션)
     *
     * <p>persist 후 seller 객체를 반환합니다. persist 과정에서 seller가 수정될 수 있으므로 원본 seller 객체를 그대로 반환합니다.
     *
     * @param seller 저장할 Seller
     * @return 저장된 Seller (persist 과정에서 수정된 상태)
     */
    public Seller persist(Seller seller) {
        persistencePort.persist(seller);
        return seller;
    }
}
