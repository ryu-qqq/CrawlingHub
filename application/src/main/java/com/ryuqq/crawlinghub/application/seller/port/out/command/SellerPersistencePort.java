package com.ryuqq.crawlinghub.application.seller.port.out.command;

import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * Seller Persistence Port
 *
 * <p>Seller Aggregate 영속성 관리 Port
 *
 * <p>단일 persist() 메서드로 생성/수정 모두 처리 (JPA ID 유무로 자동 판단)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SellerPersistencePort {

    /**
     * Seller 영속화
     *
     * <p>생성과 수정을 구분하지 않음 (JPA가 ID 유무로 판단)
     *
     * @param seller 영속화할 Seller Aggregate
     * @return 저장된 Seller의 ID
     */
    SellerId persist(Seller seller);
}
