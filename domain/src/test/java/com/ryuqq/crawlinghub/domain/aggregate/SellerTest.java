package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.SellerId;
import com.ryuqq.crawlinghub.domain.vo.SellerStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Seller Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - Seller 생성 (register) 테스트
 * - Seller 주기 변경 (updateInterval) 테스트
 */
class SellerTest {

    @Test
    void shouldRegisterSellerWithValidData() {
        SellerId sellerId = new SellerId("seller_123");
        String name = "테스트 셀러";
        Integer intervalDays = 1;

        Seller seller = Seller.register(sellerId, name, intervalDays);

        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        assertThat(seller.getCrawlingIntervalDays()).isEqualTo(1);
    }

    @Test
    void shouldUpdateCrawlingInterval() {
        // Given
        SellerId sellerId = new SellerId("seller_001");
        Seller seller = Seller.register(sellerId, "테스트 셀러", 1);
        Integer newIntervalDays = 7;

        // When
        seller.updateInterval(newIntervalDays);

        // Then
        assertThat(seller.getCrawlingIntervalDays()).isEqualTo(7);
    }

    @Test
    void shouldThrowExceptionWhenUpdateIntervalWithInvalidDays() {
        // Given
        SellerId sellerId = new SellerId("seller_002");
        Seller seller = Seller.register(sellerId, "테스트 셀러", 1);

        // When & Then
        assertThatThrownBy(() -> seller.updateInterval(31))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("크롤링 주기는 1-30일 사이여야 합니다");
    }
}
