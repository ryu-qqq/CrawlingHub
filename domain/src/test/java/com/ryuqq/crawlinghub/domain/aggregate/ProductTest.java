package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.vo.ItemNo;
import com.ryuqq.crawlinghub.domain.vo.SellerId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Product Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - Product 생성 (create) 테스트
 */
class ProductTest {

    @Test
    void shouldCreateProductWithIncompleteStatus() {
        // Given
        ItemNo itemNo = new ItemNo(123456L);
        SellerId sellerId = SellerFixture.defaultSellerId();

        // When
        Product product = Product.create(itemNo, sellerId);

        // Then
        assertThat(product.getProductId()).isNotNull();
        assertThat(product.getItemNo()).isEqualTo(itemNo);
        assertThat(product.getSellerId()).isEqualTo(sellerId);
        assertThat(product.isComplete()).isFalse();
    }
}
