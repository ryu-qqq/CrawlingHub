package com.ryuqq.crawlinghub.application.seller.port.out.command;

import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;

public interface SellerPersistPort {

    SellerId persist(Seller seller);
}
