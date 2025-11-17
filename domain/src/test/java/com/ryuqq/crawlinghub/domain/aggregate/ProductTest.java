package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.product.aggregate.product.Product;
import com.ryuqq.crawlinghub.domain.product.vo.*;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.fixture.ProductFixture;
import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.product.vo.ItemNo;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Product Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - Product 생성 (create) 테스트
 * - Product 데이터 업데이트 및 해시 계산 테스트
 * - Product 변경 감지 (Tell Don't Ask) 테스트
 * - 리팩토링: 정적 팩토리 메서드 패턴 (forNew/of/reconstitute) 테스트
 * - 리팩토링: Clock 의존성 테스트 (테스트 가능성)
 */
class ProductTest {

    // ========== Clock 고정 (테스트 재현성) ==========

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2024-01-01T00:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    // ========== 리팩토링: Clock 의존성 테스트 ==========

    @Test
    void shouldCreateProductWithFixedClock() {
        // Given
        ItemNo itemNo = new ItemNo(123456L);
        SellerId sellerId = SellerFixture.defaultSellerId();
        LocalDateTime expectedTime = LocalDateTime.now(FIXED_CLOCK);

        // When
        Product product = Product.forNew(itemNo, sellerId, FIXED_CLOCK);

        // Then
        assertThat(product.getCreatedAt()).isEqualTo(expectedTime);
        assertThat(product.getUpdatedAt()).isEqualTo(expectedTime);
    }

    @Test
    void shouldPreserveCreatedAtWhenDataChanges() {
        // Given
        ItemNo itemNo = new ItemNo(123456L);
        SellerId sellerId = SellerFixture.defaultSellerId();

        Product product = Product.forNew(itemNo, sellerId, FIXED_CLOCK);
        LocalDateTime createdTime = product.getCreatedAt();

        // When - 데이터 업데이트
        product.updateMinishopData("{\"data\":\"test\"}");

        // Then - createdAt은 불변, updatedAt은 갱신됨
        assertThat(product.getCreatedAt()).isEqualTo(createdTime);
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    // ========== 리팩토링: 정적 팩토리 메서드 패턴 테스트 ==========

    @Test
    void shouldCreateProductUsingForNew() {
        // Given
        ItemNo itemNo = new ItemNo(123456L);
        SellerId sellerId = SellerFixture.defaultSellerId();

        // When
        Product product = Product.forNew(itemNo, sellerId);

        // Then
        assertThat(product.getProductId()).isNotNull();
        assertThat(product.getItemNo()).isEqualTo(itemNo);
        assertThat(product.getSellerId()).isEqualTo(sellerId);
        assertThat(product.isComplete()).isFalse();
    }

    @Test
    void shouldCreateProductUsingOf() {
        // Given
        ItemNo itemNo = new ItemNo(123456L);
        SellerId sellerId = SellerFixture.defaultSellerId();

        // When
        Product product = Product.of(itemNo, sellerId);

        // Then
        assertThat(product.getProductId()).isNotNull();
        assertThat(product.getItemNo()).isEqualTo(itemNo);
        assertThat(product.getSellerId()).isEqualTo(sellerId);
        assertThat(product.isComplete()).isFalse();
    }

    @Test
    void shouldReconstituteProductWithAllFields() {
        // Given
        ItemNo itemNo = new ItemNo(123456L);
        SellerId sellerId = SellerFixture.defaultSellerId();
        String minishopDataHash = "abc123";
        String detailDataHash = "def456";
        String optionDataHash = "ghi789";
        Boolean isComplete = true;

        // When
        Product product = Product.reconstitute(itemNo, sellerId, minishopDataHash, detailDataHash, optionDataHash, isComplete);

        // Then
        assertThat(product.getItemNo()).isEqualTo(itemNo);
        assertThat(product.getSellerId()).isEqualTo(sellerId);
        assertThat(product.getMinishopDataHash()).isEqualTo(minishopDataHash);
        assertThat(product.isComplete()).isTrue();
    }

    // ========== 기존 테스트 (레거시, 유지보수용) ==========

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
