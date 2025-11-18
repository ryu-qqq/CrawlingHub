package com.ryuqq.crawlinghub.application.seller.port.out.command;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;

/**
 * Seller Command Port (CUD 작업)
 */
public interface SellerCommandPort {

    /**
     * Seller 저장
     *
     * @param seller Seller Aggregate
     */
    void save(Seller seller);

    /**
     * Seller 삭제
     *
     * @param seller Seller Aggregate
     */
    void delete(Seller seller);
}
