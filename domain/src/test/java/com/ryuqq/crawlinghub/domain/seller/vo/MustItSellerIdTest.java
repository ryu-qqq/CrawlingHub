package com.ryuqq.crawlinghub.domain.seller.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MustItSellerId VO 테스트")
class MustItSellerIdTest {

    @Test
    @DisplayName("of()는 null을 허용하지 않는다")
    void shouldCreateMustItSellerId() {
        MustItSellerId mustItSellerId = MustItSellerId.of(3000L);

        assertEquals(3000L, mustItSellerId.value());
    }

    @Test
    @DisplayName("forNew()는 신규 상태를 표현한다")
    void shouldRepresentNewMustItSellerId() {
        MustItSellerId mustItSellerId = MustItSellerId.forNew();

        assertTrue(mustItSellerId.isNew());
    }
}

