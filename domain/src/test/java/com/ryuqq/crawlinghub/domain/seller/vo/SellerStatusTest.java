package com.ryuqq.crawlinghub.domain.seller.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ryuqq.crawlinghub.domain.fixture.seller.SellerStatusFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SellerStatus Enum 테스트")
class SellerStatusTest {

    @Test
    @DisplayName("ACTIVE 상태를 제공해야 한다")
    void shouldHaveActiveStatus() {
        assertEquals(SellerStatus.ACTIVE, SellerStatusFixture.active());
    }

    @Test
    @DisplayName("INACTIVE 상태를 제공해야 한다")
    void shouldHaveInactiveStatus() {
        assertEquals(SellerStatus.INACTIVE, SellerStatusFixture.inactive());
    }
}

