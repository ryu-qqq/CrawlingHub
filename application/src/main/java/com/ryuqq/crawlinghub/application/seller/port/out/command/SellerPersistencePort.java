package com.ryuqq.crawlinghub.application.seller.port.out.command;

import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * Seller Aggregate 영속화를 담당하는 Command Outbound Port.
 *
 * <p>Persistence Adapter는 이 Port를 구현하여 신규/수정 로직을 처리합니다.</p>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SellerPersistencePort {

    /**
     * Seller Aggregate를 저장한다.
     *
     * <p>ID가 없으면 신규 생성, 있으면 수정으로 처리한다.</p>
     *
     * @param seller 저장할 Seller Aggregate
     * @return 저장된 Seller의 ID Value Object
     */
    SellerId persist(Seller seller);
}

