package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.fixture.ProductFixture;
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
 * - Product 데이터 업데이트 및 해시 계산 테스트
 * - Product 변경 감지 (Tell Don't Ask) 테스트
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

    @Test
    void shouldUpdateMinishopDataWithHash() {
        // Given
        Product product = ProductFixture.defaultProduct();
        String rawJson = "{\"itemNo\":123456,\"name\":\"상품명\"}";

        // When
        boolean hasChanged = product.updateMinishopData(rawJson);

        // Then
        assertThat(product.getMinishopDataHash()).isNotNull();
        assertThat(hasChanged).isTrue();
    }

    @Test
    void shouldDetectNoChangeWhenSameData() {
        // Given
        Product product = ProductFixture.defaultProduct();
        String rawJson = "{\"itemNo\":123456}";
        product.updateMinishopData(rawJson);
        String sameJson = "{\"itemNo\":123456}";

        // When
        boolean hasChanged = product.updateMinishopData(sameJson);

        // Then
        assertThat(hasChanged).isFalse();
    }

    @Test
    void shouldMarkCompleteWhenAllDataUpdated() {
        // Given
        Product product = ProductFixture.defaultProduct();

        // When
        product.updateMinishopData("{\"data\":\"minishop\"}");
        product.updateDetailData("{\"data\":\"detail\"}");
        product.updateOptionData("{\"data\":\"option\"}");

        // Then
        assertThat(product.isComplete()).isTrue();
    }

    @Test
    void shouldDetectChange() {
        // Given
        String oldHash = "abc123";
        String newHash = "def456";

        // When
        boolean hasChanged = Product.hasChanged(oldHash, newHash);

        // Then
        assertThat(hasChanged).isTrue();
    }

    @Test
    void shouldDetectNoChange() {
        // Given
        String sameHash = "abc123";

        // When
        boolean hasChanged = Product.hasChanged(sameHash, sameHash);

        // Then
        assertThat(hasChanged).isFalse();
    }
}
