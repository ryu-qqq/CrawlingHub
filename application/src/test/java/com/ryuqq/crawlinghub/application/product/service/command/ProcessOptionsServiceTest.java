package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.facade.OptionProcessFacade;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProcessOptionsService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessOptionsService 테스트")
class ProcessOptionsServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductReadManager crawledProductReadManager;

    @Mock private CrawledProductFactory crawledProductFactory;

    @Mock private OptionProcessFacade optionProcessFacade;

    private ProcessOptionsService service;

    @BeforeEach
    void setUp() {
        service =
                new ProcessOptionsService(
                        crawledProductReadManager, crawledProductFactory, optionProcessFacade);
    }

    @Nested
    @DisplayName("process() 테스트")
    class Process {

        @Test
        @DisplayName("[성공] 기존 상품 존재 → updateAndRequestSync 호출")
        void shouldUpdateProductWhenExists() {
            // Given
            List<ProductOption> options = createProductOptions();
            OptionCrawlData crawlData = createOptionCrawlData();
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createUpdatedMockProduct();

            given(crawledProductReadManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.of(existingProduct));
            given(crawledProductFactory.createOptionCrawlData(options)).willReturn(crawlData);
            given(optionProcessFacade.updateAndRequestSync(existingProduct, crawlData))
                    .willReturn(updatedProduct);

            // When
            Optional<CrawledProduct> result = service.process(SELLER_ID, ITEM_NO, options);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(PRODUCT_ID);

            // updateAndRequestSync 호출 검증
            verify(optionProcessFacade, times(1)).updateAndRequestSync(existingProduct, crawlData);
        }

        @Test
        @DisplayName("[성공] 상품 미존재 → Optional.empty() 반환")
        void shouldReturnEmptyWhenProductNotExists() {
            // Given
            List<ProductOption> options = createProductOptions();

            given(crawledProductReadManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.empty());

            // When
            Optional<CrawledProduct> result = service.process(SELLER_ID, ITEM_NO, options);

            // Then
            assertThat(result).isEmpty();

            // Factory 및 Facade 호출 안 됨 검증
            verify(crawledProductFactory, never()).createOptionCrawlData(any());
            verify(optionProcessFacade, never())
                    .updateAndRequestSync(any(), any(OptionCrawlData.class));
        }

        @Test
        @DisplayName("[성공] 빈 옵션 리스트 → 정상 처리")
        void shouldProcessEmptyOptions() {
            // Given
            List<ProductOption> options = List.of();
            OptionCrawlData crawlData = createEmptyOptionCrawlData();
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();

            given(crawledProductReadManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.of(existingProduct));
            given(crawledProductFactory.createOptionCrawlData(options)).willReturn(crawlData);
            given(optionProcessFacade.updateAndRequestSync(existingProduct, crawlData))
                    .willReturn(updatedProduct);

            // When
            Optional<CrawledProduct> result = service.process(SELLER_ID, ITEM_NO, options);

            // Then
            assertThat(result).isPresent();

            verify(optionProcessFacade, times(1)).updateAndRequestSync(existingProduct, crawlData);
        }
    }

    // === Helper Methods ===

    private List<ProductOption> createProductOptions() {
        return List.of(
                ProductOption.of(1001L, ITEM_NO, "Black", "S", 10, ""),
                ProductOption.of(1002L, ITEM_NO, "Black", "M", 20, ""),
                ProductOption.of(1003L, ITEM_NO, "White", "S", 5, ""));
    }

    private OptionCrawlData createOptionCrawlData() {
        ProductOptions productOptions = ProductOptions.from(createProductOptions());
        return OptionCrawlData.of(productOptions, FIXED_CLOCK.instant());
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
                ProductPrice.of(10000, 12000, 12000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"),
                null,
                "<p>Test Description</p>",
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
                ProductPrice.of(10000, 12000, 12000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"),
                null,
                "<p>Test Description</p>",
                "<p>Test Description</p>",
                "ACTIVE",
                "Korea",
                "Seoul",
                ProductOptions.from(createProductOptions()),
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
