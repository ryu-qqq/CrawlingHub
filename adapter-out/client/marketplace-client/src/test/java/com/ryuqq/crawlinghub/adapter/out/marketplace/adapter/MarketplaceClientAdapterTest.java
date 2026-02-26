package com.ryuqq.crawlinghub.adapter.out.marketplace.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.crawlinghub.adapter.out.marketplace.strategy.ProductSyncStrategy;
import com.ryuqq.crawlinghub.adapter.out.marketplace.strategy.ProductSyncStrategyProvider;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductSyncOutboxId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * MarketplaceClientAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MarketplaceClientAdapter 테스트")
class MarketplaceClientAdapterTest {

    @Mock private ProductSyncStrategyProvider strategyProvider;

    @Mock private ProductSyncStrategy productSyncStrategy;

    private MarketplaceClientAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MarketplaceClientAdapter(strategyProvider);
    }

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

    private CrawledProduct createCrawledProduct() {
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

    private Seller createSeller() {
        return Seller.reconstitute(
                SellerId.of(200L),
                MustItSellerName.of("mustit-seller"),
                SellerName.of("test-seller"),
                999L,
                com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus.ACTIVE,
                0,
                Instant.now(),
                Instant.now());
    }

    @Nested
    @DisplayName("sync 메서드 테스트")
    class SyncTest {

        @Test
        @DisplayName("sync 호출 시 strategyProvider에서 전략을 조회한다")
        void sync_withCreateType_delegatesToStrategyProvider() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.CREATE);
            CrawledProduct product = createCrawledProduct();
            Seller seller = createSeller();
            ProductSyncResult expectedResult = ProductSyncResult.success(1000001L);

            when(strategyProvider.getStrategy(SyncType.CREATE)).thenReturn(productSyncStrategy);
            when(productSyncStrategy.execute(any(), any(), any())).thenReturn(expectedResult);

            // when
            ProductSyncResult result = adapter.sync(outbox, product, seller);

            // then
            assertThat(result).isNotNull();
            assertThat(result.success()).isTrue();
            assertThat(result.externalProductId()).isEqualTo(1000001L);
            verify(strategyProvider).getStrategy(SyncType.CREATE);
            verify(productSyncStrategy).execute(outbox, product, seller);
        }

        @Test
        @DisplayName("UPDATE_PRICE SyncType으로 sync 호출 시 해당 전략이 실행된다")
        void sync_withUpdatePriceType_executesUpdatePriceStrategy() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.UPDATE_PRICE);
            CrawledProduct product = createCrawledProduct();
            Seller seller = createSeller();
            ProductSyncResult expectedResult = ProductSyncResult.success(99999L);

            when(strategyProvider.getStrategy(SyncType.UPDATE_PRICE))
                    .thenReturn(productSyncStrategy);
            when(productSyncStrategy.execute(any(), any(), any())).thenReturn(expectedResult);

            // when
            ProductSyncResult result = adapter.sync(outbox, product, seller);

            // then
            assertThat(result.success()).isTrue();
            verify(strategyProvider).getStrategy(SyncType.UPDATE_PRICE);
        }

        @Test
        @DisplayName("strategyProvider가 예외를 던지면 그대로 전파된다")
        void sync_whenStrategyProviderThrows_propagatesException() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox(SyncType.CREATE);
            CrawledProduct product = createCrawledProduct();
            Seller seller = createSeller();

            when(strategyProvider.getStrategy(any()))
                    .thenThrow(new IllegalArgumentException("지원하지 않는 SyncType"));

            // when & then
            org.assertj.core.api.Assertions.assertThatThrownBy(
                            () -> adapter.sync(outbox, product, seller))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원하지 않는 SyncType");
        }
    }
}
