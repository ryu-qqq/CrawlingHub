package com.ryuqq.crawlinghub.application.seller.port.out.query;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

import java.util.Optional;

/**
 * Seller Query Port (R 작업)
 */
public interface SellerQueryPort {

    /**
     * SellerId로 Seller 조회
     *
     * @param sellerId SellerId VO
     * @return Seller Optional
     */
    Optional<Seller> findBySellerId(SellerId sellerId);

    /**
     * 이름으로 Seller 존재 여부 확인
     *
     * @param name Seller 이름
     * @return 존재 여부
     */
    boolean existsByName(String name);
}
