package com.ryuqq.crawlinghub.application.fixture.seller.query;

import com.ryuqq.crawlinghub.application.seller.dto.query.ListSellersQuery;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

public final class ListSellersQueryFixture {

    private ListSellersQueryFixture() {
    }

    public static ListSellersQuery activePageRequest() {
        return new ListSellersQuery(SellerStatus.ACTIVE, 0, 20);
    }

    public static ListSellersQuery inactivePageRequest() {
        return new ListSellersQuery(SellerStatus.INACTIVE, 1, 50);
    }
}

