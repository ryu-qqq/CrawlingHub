package com.ryuqq.crawlinghub.application.port.out.query;

import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import java.util.Optional;

/**
 * Seller Query Port
 */
public interface SellerQueryPort {

    Optional<Seller> findById(SellerId sellerId);
}

