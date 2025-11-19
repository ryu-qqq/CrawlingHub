package com.ryuqq.crawlinghub.application.fixture.seller.response;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

public final class SellerDetailResponseFixture {

    private SellerDetailResponseFixture() {
    }

    public static SellerDetailResponse sample() {
        return new SellerDetailResponse(
            1L,
            "mustit-seller-001",
            "머스트잇 셀러",
            SellerStatus.ACTIVE,
            2,
            5,
            LocalDateTime.parse("2025-01-01T00:00:00"),
            LocalDateTime.parse("2025-01-02T00:00:00")
        );
    }
}

