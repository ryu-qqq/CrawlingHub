package com.ryuqq.crawlinghub.application.fixture.seller.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;

public final class RegisterSellerCommandFixture {

    private RegisterSellerCommandFixture() {
    }

    public static RegisterSellerCommand sample() {
        return new RegisterSellerCommand(
            1001L,
            "머스트잇 셀러"
        );
    }
}

