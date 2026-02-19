package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.dto.bundle.DetailProcessBundle;
import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.application.product.facade.DetailProcessFacade;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
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
 * ProcessDetailInfoService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessDetailInfoService 테스트")
class ProcessDetailInfoServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductReadManager crawledProductReadManager;

    @Mock private CrawledProductFactory crawledProductFactory;

    @Mock private DetailProcessFacade detailProcessFacade;

    private ProcessDetailInfoService service;

    @BeforeEach
    void setUp() {
        service =
                new ProcessDetailInfoService(
                        crawledProductReadManager, crawledProductFactory, detailProcessFacade);
    }

    @Nested
    @DisplayName("process() 테스트")
    class Process {

        @Test
        @DisplayName("[성공] 기존 상품 존재 → updateAndRequestUploadAndSync 호출")
        void shouldUpdateProductWhenExists() {
            // Given
            ProductDetailInfo detailInfo = createProductDetailInfo();
            DetailProcessBundle bundle = createProcessBundle();
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createUpdatedMockProduct();

            given(crawledProductReadManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.of(existingProduct));
            given(crawledProductFactory.createDetailProcessBundle(detailInfo)).willReturn(bundle);
            given(detailProcessFacade.updateAndRequestUploadAndSync(existingProduct, bundle))
                    .willReturn(updatedProduct);

            // When
            Optional<CrawledProduct> result = service.process(SELLER_ID, ITEM_NO, detailInfo);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(PRODUCT_ID);

            // updateAndRequestUploadAndSync 호출 검증
            verify(detailProcessFacade, times(1))
                    .updateAndRequestUploadAndSync(existingProduct, bundle);
        }

        @Test
        @DisplayName("[성공] 상품 미존재 → Optional.empty() 반환")
        void shouldReturnEmptyWhenProductNotExists() {
            // Given
            ProductDetailInfo detailInfo = createProductDetailInfo();

            given(crawledProductReadManager.findBySellerIdAndItemNo(SELLER_ID, ITEM_NO))
                    .willReturn(Optional.empty());

            // When
            Optional<CrawledProduct> result = service.process(SELLER_ID, ITEM_NO, detailInfo);

            // Then
            assertThat(result).isEmpty();

            // Factory 및 Facade 호출 안 됨 검증
            verify(crawledProductFactory, never()).createDetailProcessBundle(any());
            verify(detailProcessFacade, never())
                    .updateAndRequestUploadAndSync(any(), any(DetailProcessBundle.class));
        }
    }

    // === Helper Methods ===

    private ProductDetailInfo createProductDetailInfo() {
        return ProductDetailInfo.builder()
                .sellerNo(SELLER_ID.value())
                .sellerId("seller123")
                .itemNo(ITEM_NO)
                .itemName("Test Product")
                .brandName("Test Brand")
                .brandNameKr("테스트 브랜드")
                .brandCode(1001L)
                .category(ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"))
                .normalPrice(12000)
                .sellingPrice(10000)
                .discountPrice(9000)
                .discountRate(10)
                .stock(100)
                .isSoldOut(false)
                .shipping(null)
                .bannerImages(List.of())
                .detailImages(
                        List.of(
                                "https://example.com/detail1.jpg",
                                "https://example.com/detail2.jpg"))
                .descriptionMarkUp("<p>Test Description</p>")
                .originCountry("Korea")
                .itemStatus("ACTIVE")
                .build();
    }

    private DetailProcessBundle createProcessBundle() {
        DetailCrawlData crawlData =
                DetailCrawlData.of(
                        ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"),
                        null,
                        "<p>Test Description</p>",
                        "ACTIVE",
                        "Korea",
                        null,
                        List.of(
                                "https://example.com/detail1.jpg",
                                "https://example.com/detail2.jpg"),
                        FIXED_INSTANT);

        ImageUploadData imageUploadData =
                ImageUploadData.of(
                        List.of(
                                "https://example.com/detail1.jpg",
                                "https://example.com/detail2.jpg"),
                        ImageType.DESCRIPTION);

        return new DetailProcessBundle(crawlData, imageUploadData);
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

    private CrawledProduct createUpdatedMockProduct() {
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
}
