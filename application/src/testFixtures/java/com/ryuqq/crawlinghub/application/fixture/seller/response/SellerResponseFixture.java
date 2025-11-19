package com.ryuqq.crawlinghub.application.fixture.seller.response;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

public final class SellerResponseFixture {

    private SellerResponseFixture() {
    }

    public static SellerResponse sample() {
        return new SellerResponse(
            1L,
            "mustit-seller-001",
            "머스트잇 셀러",
            SellerStatus.ACTIVE,
            LocalDateTime.parse("2025-01-01T00:00:00")
        );
    }
}

