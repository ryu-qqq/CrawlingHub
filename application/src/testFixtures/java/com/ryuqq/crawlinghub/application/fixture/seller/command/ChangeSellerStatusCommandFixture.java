package com.ryuqq.crawlinghub.application.fixture.seller.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.ChangeSellerStatusCommand;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

public final class ChangeSellerStatusCommandFixture {

    private ChangeSellerStatusCommandFixture() {
    }

    public static ChangeSellerStatusCommand deactivateRequest() {
        return new ChangeSellerStatusCommand(1L, SellerStatus.INACTIVE);
    }

    public static ChangeSellerStatusCommand activateRequest() {
        return new ChangeSellerStatusCommand(1L, SellerStatus.ACTIVE);
    }
}

