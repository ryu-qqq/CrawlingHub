package com.ryuqq.crawlinghub.application.fixture.seller.response;

import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

public final class SellerSummaryResponseFixture {

    private SellerSummaryResponseFixture() {
    }

    public static SellerSummaryResponse sample() {
        return new SellerSummaryResponse(
            1L,
            "mustit-seller-001",
            "머스트잇 셀러",
            SellerStatus.ACTIVE,
            5
        );
    }
}

