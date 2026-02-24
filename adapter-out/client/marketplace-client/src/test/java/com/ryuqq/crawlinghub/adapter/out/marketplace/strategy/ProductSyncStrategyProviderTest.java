package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ProductSyncStrategyProvider 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProductSyncStrategyProvider 테스트")
class ProductSyncStrategyProviderTest {

    @Nested
    @DisplayName("getStrategy 메서드 테스트")
    class GetStrategyTest {

        @Test
        @DisplayName("CREATE SyncType에 대한 전략을 반환한다")
        void getStrategy_withCreateType_returnsCreateStrategy() {
            // given
            List<ProductSyncStrategy> strategies =
                    List.of(
                            new CreateProductSyncStrategy(),
                            new UpdatePriceSyncStrategy(),
                            new UpdateImageSyncStrategy(),
                            new UpdateDescriptionSyncStrategy(),
                            new UpdateOptionStockSyncStrategy(),
                            new UpdateProductInfoSyncStrategy());
            ProductSyncStrategyProvider provider = new ProductSyncStrategyProvider(strategies);

            // when
            ProductSyncStrategy strategy = provider.getStrategy(SyncType.CREATE);

            // then
            assertThat(strategy).isInstanceOf(CreateProductSyncStrategy.class);
        }

        @Test
        @DisplayName("UPDATE_PRICE SyncType에 대한 전략을 반환한다")
        void getStrategy_withUpdatePriceType_returnsUpdatePriceStrategy() {
            // given
            List<ProductSyncStrategy> strategies =
                    List.of(new CreateProductSyncStrategy(), new UpdatePriceSyncStrategy());
            ProductSyncStrategyProvider provider = new ProductSyncStrategyProvider(strategies);

            // when
            ProductSyncStrategy strategy = provider.getStrategy(SyncType.UPDATE_PRICE);

            // then
            assertThat(strategy).isInstanceOf(UpdatePriceSyncStrategy.class);
        }

        @Test
        @DisplayName("UPDATE_IMAGE SyncType에 대한 전략을 반환한다")
        void getStrategy_withUpdateImageType_returnsUpdateImageStrategy() {
            // given
            List<ProductSyncStrategy> strategies = List.of(new UpdateImageSyncStrategy());
            ProductSyncStrategyProvider provider = new ProductSyncStrategyProvider(strategies);

            // when
            ProductSyncStrategy strategy = provider.getStrategy(SyncType.UPDATE_IMAGE);

            // then
            assertThat(strategy).isInstanceOf(UpdateImageSyncStrategy.class);
        }

        @Test
        @DisplayName("UPDATE_DESCRIPTION SyncType에 대한 전략을 반환한다")
        void getStrategy_withUpdateDescriptionType_returnsUpdateDescriptionStrategy() {
            // given
            List<ProductSyncStrategy> strategies = List.of(new UpdateDescriptionSyncStrategy());
            ProductSyncStrategyProvider provider = new ProductSyncStrategyProvider(strategies);

            // when
            ProductSyncStrategy strategy = provider.getStrategy(SyncType.UPDATE_DESCRIPTION);

            // then
            assertThat(strategy).isInstanceOf(UpdateDescriptionSyncStrategy.class);
        }

        @Test
        @DisplayName("UPDATE_OPTION_STOCK SyncType에 대한 전략을 반환한다")
        void getStrategy_withUpdateOptionStockType_returnsUpdateOptionStockStrategy() {
            // given
            List<ProductSyncStrategy> strategies = List.of(new UpdateOptionStockSyncStrategy());
            ProductSyncStrategyProvider provider = new ProductSyncStrategyProvider(strategies);

            // when
            ProductSyncStrategy strategy = provider.getStrategy(SyncType.UPDATE_OPTION_STOCK);

            // then
            assertThat(strategy).isInstanceOf(UpdateOptionStockSyncStrategy.class);
        }

        @Test
        @DisplayName("UPDATE_PRODUCT_INFO SyncType에 대한 전략을 반환한다")
        void getStrategy_withUpdateProductInfoType_returnsUpdateProductInfoStrategy() {
            // given
            List<ProductSyncStrategy> strategies = List.of(new UpdateProductInfoSyncStrategy());
            ProductSyncStrategyProvider provider = new ProductSyncStrategyProvider(strategies);

            // when
            ProductSyncStrategy strategy = provider.getStrategy(SyncType.UPDATE_PRODUCT_INFO);

            // then
            assertThat(strategy).isInstanceOf(UpdateProductInfoSyncStrategy.class);
        }

        @Test
        @DisplayName("등록되지 않은 SyncType 조회 시 IllegalArgumentException이 발생한다")
        void getStrategy_withUnregisteredType_throwsIllegalArgumentException() {
            // given - 빈 전략 목록
            List<ProductSyncStrategy> strategies = List.of();
            ProductSyncStrategyProvider provider = new ProductSyncStrategyProvider(strategies);

            // when & then
            assertThatThrownBy(() -> provider.getStrategy(SyncType.CREATE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원하지 않는 SyncType");
        }
    }
}
