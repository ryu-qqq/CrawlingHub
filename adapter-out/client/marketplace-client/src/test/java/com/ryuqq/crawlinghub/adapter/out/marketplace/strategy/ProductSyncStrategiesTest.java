package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ryuqq.crawlinghub.adapter.out.marketplace.client.MarketPlaceClient;
import com.ryuqq.crawlinghub.adapter.out.marketplace.mapper.CreateProductRequestMapper;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductSyncOutboxId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 모든 ProductSyncStrategy 구현체 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProductSyncStrategy 구현체 테스트")
class ProductSyncStrategiesTest {

    // ===== 테스트 헬퍼 메서드 =====

    private CrawledProductSyncOutbox createOutbox(SyncType syncType) {
        Long externalProductId = syncType.isUpdate() ? 99999L : null;
        return CrawledProductSyncOutbox.reconstitute(
                CrawledProductSyncOutboxId.of(1L),
                CrawledProductId.of(100L),
                SellerId.of(200L),
                12345L,
                syncType,
                "test-idem-key",
                externalProductId,
                ProductOutboxStatus.PENDING,
                0,
                null,
                Instant.now(),
                null);
    }

    private CrawledProduct createProduct() {
        return CrawledProduct.fromMiniShop(
                SellerId.of(200L),
                12345L,
                "테스트 상품명",
                "테스트 브랜드",
                new ProductPrice(10000, 0, 12000, 0, 0, 0),
                null,
                false,
                Instant.now());
    }

    @Nested
    @DisplayName("CreateProductSyncStrategy 테스트")
    class CreateProductSyncStrategyTest {

        private final CreateProductSyncStrategy strategy =
                new CreateProductSyncStrategy(
                        mock(MarketPlaceClient.class), mock(CreateProductRequestMapper.class));

        @Test
        @DisplayName("supportedType은 CREATE를 반환한다")
        void supportedType_returnsCreate() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.CREATE);
        }
    }

    @Nested
    @DisplayName("UpdatePriceSyncStrategy 테스트")
    class UpdatePriceSyncStrategyTest {

        private final UpdatePriceSyncStrategy strategy = new UpdatePriceSyncStrategy();

        @Test
        @DisplayName("supportedType은 UPDATE_PRICE를 반환한다")
        void supportedType_returnsUpdatePrice() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_PRICE);
        }

        @Test
        @DisplayName("execute는 성공 결과와 기존 externalProductId를 반환한다")
        void execute_returnsSuccessWithExistingExternalProductId() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_PRICE);
            CrawledProduct product = createProduct();

            // when
            ProductSyncResult result = strategy.execute(outbox, product, null);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
        }
    }

    @Nested
    @DisplayName("UpdateImageSyncStrategy 테스트")
    class UpdateImageSyncStrategyTest {

        private final UpdateImageSyncStrategy strategy = new UpdateImageSyncStrategy();

        @Test
        @DisplayName("supportedType은 UPDATE_IMAGE를 반환한다")
        void supportedType_returnsUpdateImage() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_IMAGE);
        }

        @Test
        @DisplayName("execute는 성공 결과를 반환한다")
        void execute_returnsSuccess() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_IMAGE);
            CrawledProduct product = createProduct();

            // when
            ProductSyncResult result = strategy.execute(outbox, product, null);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
        }
    }

    @Nested
    @DisplayName("UpdateDescriptionSyncStrategy 테스트")
    class UpdateDescriptionSyncStrategyTest {

        private final UpdateDescriptionSyncStrategy strategy = new UpdateDescriptionSyncStrategy();

        @Test
        @DisplayName("supportedType은 UPDATE_DESCRIPTION을 반환한다")
        void supportedType_returnsUpdateDescription() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_DESCRIPTION);
        }

        @Test
        @DisplayName("execute는 성공 결과를 반환한다")
        void execute_returnsSuccess() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_DESCRIPTION);
            CrawledProduct product = createProduct();

            // when
            ProductSyncResult result = strategy.execute(outbox, product, null);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
        }
    }

    @Nested
    @DisplayName("UpdateOptionStockSyncStrategy 테스트")
    class UpdateOptionStockSyncStrategyTest {

        private final UpdateOptionStockSyncStrategy strategy = new UpdateOptionStockSyncStrategy();

        @Test
        @DisplayName("supportedType은 UPDATE_OPTION_STOCK을 반환한다")
        void supportedType_returnsUpdateOptionStock() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_OPTION_STOCK);
        }

        @Test
        @DisplayName("execute는 성공 결과를 반환한다")
        void execute_returnsSuccess() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_OPTION_STOCK);
            CrawledProduct product = createProduct();

            // when
            ProductSyncResult result = strategy.execute(outbox, product, null);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
        }
    }

    @Nested
    @DisplayName("UpdateProductInfoSyncStrategy 테스트")
    class UpdateProductInfoSyncStrategyTest {

        private final UpdateProductInfoSyncStrategy strategy = new UpdateProductInfoSyncStrategy();

        @Test
        @DisplayName("supportedType은 UPDATE_PRODUCT_INFO를 반환한다")
        void supportedType_returnsUpdateProductInfo() {
            assertThat(strategy.supportedType()).isEqualTo(SyncType.UPDATE_PRODUCT_INFO);
        }

        @Test
        @DisplayName("execute는 성공 결과를 반환한다")
        void execute_returnsSuccess() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_PRODUCT_INFO);
            CrawledProduct product = createProduct();

            // when
            ProductSyncResult result = strategy.execute(outbox, product, null);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(99999L);
        }
    }
}
