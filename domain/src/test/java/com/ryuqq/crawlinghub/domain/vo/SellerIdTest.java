package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SellerId Value Object 테스트
 *
 * TDD Phase: Red
 * - SellerId VO 생성 검증
 * - null/blank 검증
 */
class SellerIdTest {

    @Test
    void shouldCreateSellerIdWithValidValue() {
        String validSellerId = "seller_123";
        SellerId sellerId = new SellerId(validSellerId);
        assertThat(sellerId.value()).isEqualTo(validSellerId);
    }

    @Test
    void shouldThrowExceptionWhenSellerIdIsBlank() {
        assertThatThrownBy(() -> new SellerId(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SellerId는 비어있을 수 없습니다");
    }
}
