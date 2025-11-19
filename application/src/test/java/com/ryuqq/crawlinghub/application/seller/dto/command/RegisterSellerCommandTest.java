package com.ryuqq.crawlinghub.application.seller.dto.command;

import com.ryuqq.crawlinghub.application.fixture.seller.SellerCommandFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegisterSellerCommand")
class RegisterSellerCommandTest {

    @Test
    @DisplayName("머스트잇 셀러 ID와 셀러명을 보존하는 불변 DTO다")
    void shouldExposeSellerIdentification() {
        RegisterSellerCommand command = SellerCommandFixture.registerSeller();

        assertThat(command.mustItSellerId()).isEqualTo(1001L);
        assertThat(command.sellerName()).isEqualTo("머스트잇 셀러");
    }
}

