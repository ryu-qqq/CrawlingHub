package com.ryuqq.crawlinghub.domain.seller.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SellerId VO 테스트")
class SellerIdTest {

    @Test
    @DisplayName("of()는 값을 보존한다")
    void shouldCreateSellerIdWithValue() {
        SellerId sellerId = SellerId.of(1L);

        assertEquals(1L, sellerId.value());
    }

    @Test
    @DisplayName("forNew()는 신규 상태를 표현한다")
    void shouldRepresentNewSellerId() {
        SellerId sellerId = SellerId.forNew();

        assertTrue(sellerId.isNew());
    }
}

