package com.ryuqq.crawlinghub.application.product.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.application.sync.port.in.command.RequestSyncUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * OptionProcessFacade 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OptionProcessFacade 테스트")
class OptionProcessFacadeTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductTransactionManager crawledProductManager;

    @Mock private RequestSyncUseCase requestSyncUseCase;

    private OptionProcessFacade facade;

    @BeforeEach
    void setUp() {
        facade = new OptionProcessFacade(crawledProductManager, requestSyncUseCase);
    }

    @Nested
    @DisplayName("updateAndRequestSync() 테스트")
    class UpdateAndRequestSync {

        @Test
        @DisplayName("[성공] Product 업데이트 + Sync 요청 수행")
        void shouldUpdateProductAndRequestSync() {
            // Given
            OptionCrawlData crawlData = createOptionCrawlData();
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createUpdatedMockProduct();

            given(
                            crawledProductManager.updateFromOptionCrawlData(
                                    any(CrawledProduct.class), any(OptionCrawlData.class)))
                    .willReturn(updatedProduct);

            // When
            CrawledProduct result = facade.updateAndRequestSync(existingProduct, crawlData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);

            // updateFromOptionCrawlData 호출 검증
            verify(crawledProductManager, times(1))
                    .updateFromOptionCrawlData(existingProduct, crawlData);

            // Sync 요청 검증
            verify(requestSyncUseCase, times(1)).requestIfReady(updatedProduct);
        }

        @Test
        @DisplayName("[성공] 빈 옵션으로도 정상 업데이트 수행")
        void shouldUpdateProductWithEmptyOptions() {
            // Given
            OptionCrawlData crawlData = createEmptyOptionCrawlData();
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createUpdatedMockProduct();

            given(
                            crawledProductManager.updateFromOptionCrawlData(
                                    any(CrawledProduct.class), any(OptionCrawlData.class)))
                    .willReturn(updatedProduct);

            // When
            CrawledProduct result = facade.updateAndRequestSync(existingProduct, crawlData);

            // Then
            assertThat(result).isNotNull();

            // updateFromOptionCrawlData 호출 검증
            verify(crawledProductManager, times(1))
                    .updateFromOptionCrawlData(existingProduct, crawlData);

            // Sync 요청 검증
            verify(requestSyncUseCase, times(1)).requestIfReady(updatedProduct);
        }
    }

    // === Helper Methods ===

    private OptionCrawlData createOptionCrawlData() {
        return OptionCrawlData.of(ProductOptions.empty(), FIXED_CLOCK.instant());
    }

    private OptionCrawlData createEmptyOptionCrawlData() {
        return OptionCrawlData.of(ProductOptions.empty(), FIXED_CLOCK.instant());
    }

    private CrawledProduct createMockProduct() {
        Instant now = FIXED_CLOCK.instant();
        return CrawledProduct.reconstitute(
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                "Test Product",
                "Test Brand",
                ProductPrice.of(10000, 10000, 10000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"),
                null,
                "<p>Test Description</p>",
                "ACTIVE",
                "Korea",
                "Seoul",
                ProductOptions.empty(),
                CrawlCompletionStatus.initial().withMiniShopCrawled(now).withDetailCrawled(now),
                null,
                null,
                false,
                now,
                now);
    }

    private CrawledProduct createUpdatedMockProduct() {
        Instant now = FIXED_CLOCK.instant();
        return CrawledProduct.reconstitute(
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                "Test Product",
                "Test Brand",
                ProductPrice.of(10000, 10000, 10000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"),
                null,
                "<p>Test Description</p>",
                "ACTIVE",
                "Korea",
                "Seoul",
                ProductOptions.empty(),
                CrawlCompletionStatus.initial()
                        .withMiniShopCrawled(now)
                        .withDetailCrawled(now)
                        .withOptionCrawled(now),
                null,
                null,
                false,
                now,
                now);
    }
}
