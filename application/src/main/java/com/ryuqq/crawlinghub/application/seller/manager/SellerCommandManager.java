package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Command Manager
 *
 * <p>Seller Aggregate 트랜잭션 경계 관리
 *
 * <ul>
 *   <li>영속화 + 트랜잭션 캡슐화
 *   <li>QueryPort 의존성 없음 (Service에서 조회)
 *   <li>단일 PersistencePort 의존성만 보유
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerCommandManager {

    private final SellerPersistencePort sellerPersistencePort;

    public SellerCommandManager(SellerPersistencePort sellerPersistencePort) {
        this.sellerPersistencePort = sellerPersistencePort;
    }

    /**
     * Seller 영속화
     *
     * <p>생성과 수정을 구분하지 않음 (JPA가 ID 유무로 판단)
     *
     * @param seller 영속화할 Seller Aggregate
     * @return 저장된 Seller의 ID
     */
    @Transactional
    public SellerId persist(Seller seller) {
        return sellerPersistencePort.persist(seller);
    }
}
