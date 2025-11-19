package com.ryuqq.crawlinghub.application.fixture.seller;

import com.ryuqq.crawlinghub.application.fixture.seller.query.GetSellerQueryFixture;
import com.ryuqq.crawlinghub.application.fixture.seller.query.ListSellersQueryFixture;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.query.ListSellersQuery;

/**
 * Seller Query DTO Fixture 집합.
 */
public final class SellerQueryFixture {

    private SellerQueryFixture() {
    }

    public static GetSellerQuery sellerDetail() {
        return GetSellerQueryFixture.sample();
    }

    public static ListSellersQuery activeSellerPage() {
        return ListSellersQueryFixture.activePageRequest();
    }

    public static ListSellersQuery inactiveSellerPage() {
        return ListSellersQueryFixture.inactivePageRequest();
    }
}

