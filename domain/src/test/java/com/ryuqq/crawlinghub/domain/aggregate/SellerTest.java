package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.SellerId;
import com.ryuqq.crawlinghub.domain.vo.SellerStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Seller Aggregate Root 테스트
 *
 * TDD Phase: Red
 * - Seller 생성 (register) 테스트
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
}
