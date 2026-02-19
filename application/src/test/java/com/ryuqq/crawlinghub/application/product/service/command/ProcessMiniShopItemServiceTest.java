package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.application.product.dto.bundle.MiniShopProcessBundle;
import com.ryuqq.crawlinghub.application.product.facade.MiniShopProcessFacade;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
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
 * ProcessMiniShopItemService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessMiniShopItemService 테스트")
class ProcessMiniShopItemServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductReadManager crawledProductReadManager;

    @Mock private CrawledProductFactory crawledProductFactory;

    @Mock private MiniShopProcessFacade miniShopProcessFacade;

    private ProcessMiniShopItemService service;

    @BeforeEach
    void setUp() {
        service =
                new ProcessMiniShopItemService(
                        crawledProductReadManager, crawledProductFactory, miniShopProcessFacade);
    }

    @Nested
    @DisplayName("process() 테스트")
    class Process {

        @Test
        @DisplayName("[성공] 신규 상품 → createAndRequestImageUpload 호출")
        void shouldCreateNewProductWhenNotExists() {
            // Given
            MiniShopItem item = createMiniShopItem();
            MiniShopProcessBundle bundle = createProcessBundle();
            CrawledProduct createdProduct = createMockProduct();

            given(crawledProductReadManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.empty());
            given(crawledProductFactory.createMiniShopProcessBundle(SELLER_ID, item))
                    .willReturn(bundle);
            given(miniShopProcessFacade.createAndRequestImageUpload(bundle))
                    .willReturn(createdProduct);

            // When
            CrawledProduct result = service.process(SELLER_ID, item);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);

            // createAndRequestImageUpload 호출 검증
            verify(miniShopProcessFacade, times(1)).createAndRequestImageUpload(bundle);

            // updateAndRequestImageUpload는 호출 안 됨
            verify(miniShopProcessFacade, never())
                    .updateAndRequestImageUpload(any(CrawledProduct.class), any());
        }

        @Test
        @DisplayName("[성공] 기존 상품 → updateAndRequestImageUpload 호출")
        void shouldUpdateExistingProductWhenExists() {
            // Given
            MiniShopItem item = createMiniShopItem();
            MiniShopProcessBundle bundle = createProcessBundle();
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();

            given(crawledProductReadManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.of(existingProduct));
            given(crawledProductFactory.createMiniShopProcessBundle(SELLER_ID, item))
                    .willReturn(bundle);
            given(miniShopProcessFacade.updateAndRequestImageUpload(existingProduct, bundle))
                    .willReturn(updatedProduct);

            // When
            CrawledProduct result = service.process(SELLER_ID, item);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);

            // updateAndRequestImageUpload 호출 검증
            verify(miniShopProcessFacade, times(1))
                    .updateAndRequestImageUpload(existingProduct, bundle);

            // createAndRequestImageUpload는 호출 안 됨
            verify(miniShopProcessFacade, never()).createAndRequestImageUpload(any());
        }
    }

    // === Helper Methods ===

    private MiniShopItem createMiniShopItem() {
        return new MiniShopItem(
                ITEM_NO,
                List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"),
                "Test Brand",
                "Test Product",
                10000,
                12000,
                12000,
                10,
                10,
                9000,
                List.of());
    }

    private MiniShopProcessBundle createProcessBundle() {
        MiniShopCrawlData crawlData =
                MiniShopCrawlData.of(
                        SELLER_ID,
                        ITEM_NO,
                        "Test Product",
                        "Test Brand",
                        ProductPrice.of(10000, 12000, 12000, 9000, 10, 10),
                        ProductImages.fromThumbnailUrls(List.of("https://example.com/image1.jpg")),
                        true,
                        FIXED_INSTANT);

        ImageUploadData imageUploadData =
                ImageUploadData.of(List.of("https://example.com/image1.jpg"), ImageType.THUMBNAIL);

        return new MiniShopProcessBundle(crawlData, imageUploadData);
    }

    private CrawledProduct createMockProduct() {
        Instant now = FIXED_INSTANT;
        return CrawledProduct.reconstitute(
                PRODUCT_ID,
                SELLER_ID,
                ITEM_NO,
                "Test Product",
                "Test Brand",
                ProductPrice.of(10000, 12000, 12000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                "ACTIVE",
                "Korea",
                "Seoul",
                ProductOptions.empty(),
                CrawlCompletionStatus.initial().withMiniShopCrawled(now),
                null,
                null,
                false,
                now,
                now);
    }
}
