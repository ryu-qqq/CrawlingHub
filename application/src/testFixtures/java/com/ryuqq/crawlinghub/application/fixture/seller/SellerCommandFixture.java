package com.ryuqq.crawlinghub.application.fixture.seller;

import com.ryuqq.crawlinghub.application.fixture.seller.command.ChangeSellerStatusCommandFixture;
import com.ryuqq.crawlinghub.application.fixture.seller.command.RegisterSellerCommandFixture;
import com.ryuqq.crawlinghub.application.seller.dto.command.ChangeSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;

/**
 * Seller Command DTO Fixture 집합.
 */
public final class SellerCommandFixture {

    private SellerCommandFixture() {
    }

    public static RegisterSellerCommand registerSeller() {
        return RegisterSellerCommandFixture.sample();
    }

    public static ChangeSellerStatusCommand deactivateSeller() {
        return ChangeSellerStatusCommandFixture.deactivateRequest();
    }

    public static ChangeSellerStatusCommand activateSeller() {
        return ChangeSellerStatusCommandFixture.activateRequest();
    }
}

