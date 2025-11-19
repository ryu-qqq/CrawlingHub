package com.ryuqq.crawlinghub.application.fixture.seller.query;

import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;

public final class GetSellerQueryFixture {

    private GetSellerQueryFixture() {
    }

    public static GetSellerQuery sample() {
        return new GetSellerQuery(1L);
    }
}

