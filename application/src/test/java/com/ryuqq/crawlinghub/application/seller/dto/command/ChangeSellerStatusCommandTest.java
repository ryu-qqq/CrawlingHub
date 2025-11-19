package com.ryuqq.crawlinghub.application.seller.dto.command;

import com.ryuqq.crawlinghub.application.fixture.seller.SellerCommandFixture;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChangeSellerStatusCommand")
class ChangeSellerStatusCommandTest {

    @Test
    @DisplayName("셀러 ID와 목표 상태를 제공하는 불변 DTO다")
    void shouldCarryTargetStatus() {
        ChangeSellerStatusCommand command = SellerCommandFixture.deactivateSeller();

        assertThat(command.sellerId()).isEqualTo(1L);
        assertThat(command.targetStatus()).isEqualTo(SellerStatus.INACTIVE);
    }
}

