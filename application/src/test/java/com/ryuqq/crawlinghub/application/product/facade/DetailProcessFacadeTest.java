package com.ryuqq.crawlinghub.application.product.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.image.orchestrator.ImageUploadOrchestrator;
import com.ryuqq.crawlinghub.application.product.dto.bundle.DetailProcessBundle;
import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager.DetailUpdateResult;
import com.ryuqq.crawlinghub.application.sync.port.in.command.RequestSyncUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DetailProcessFacade 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DetailProcessFacade 테스트")
class DetailProcessFacadeTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductTransactionManager crawledProductManager;

    @Mock private CrawledProductImageReadManager imageReadManager;

    @Mock private ImageUploadOrchestrator imageUploadOrchestrator;

    @Mock private RequestSyncUseCase requestSyncUseCase;

    @Captor private ArgumentCaptor<ImageUploadData> imageUploadDataCaptor;

    private DetailProcessFacade facade;

    @BeforeEach
    void setUp() {
        facade =
                new DetailProcessFacade(
                        crawledProductManager,
                        imageReadManager,
                        imageUploadOrchestrator,
                        requestSyncUseCase);
    }

    @Nested
    @DisplayName("updateAndRequestUploadAndSync() 테스트")
    class UpdateAndRequestUploadAndSync {

        @Test
        @DisplayName("[성공] 새로운 이미지가 있는 경우 → Product 업데이트 + Factory로 이미지 처리 위임 + Sync 요청")
        void shouldUpdateProductAndDelegateToFactoryAndRequestSyncWhenNewImagesExist() {
            // Given
            List<String> imageUrls =
                    List.of("https://example.com/detail1.jpg", "https://example.com/detail2.jpg");
            DetailProcessBundle bundle = createBundleWithImages(imageUrls);
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();
            DetailUpdateResult updateResult = new DetailUpdateResult(updatedProduct, imageUrls);

            given(
                            crawledProductManager.updateFromDetailCrawlData(
                                    any(CrawledProduct.class), any(DetailCrawlData.class)))
                    .willReturn(updateResult);

            // 모든 URL이 새로운 URL로 반환
            given(imageReadManager.filterNewImageUrls(any(CrawledProductId.class), anyList()))
                    .willReturn(imageUrls);

            // When
            CrawledProduct result = facade.updateAndRequestUploadAndSync(existingProduct, bundle);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);

            // Factory 호출 검증
            verify(imageUploadOrchestrator, times(1))
                    .processImageUpload(imageUploadDataCaptor.capture());
            ImageUploadData capturedData = imageUploadDataCaptor.getValue();
            assertThat(capturedData.crawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(capturedData.imageUrls()).hasSize(2);

            // Sync 요청 검증
            verify(requestSyncUseCase, times(1)).requestIfReady(updatedProduct);
        }

        @Test
        @DisplayName("[성공] 기존 URL 필터링 → 새로운 URL만 Factory에 전달")
        void shouldFilterExistingUrlsAndPassOnlyNewUrlsToFactory() {
            // Given
            List<String> allUrls =
                    List.of("https://example.com/existing.jpg", "https://example.com/new.jpg");
            List<String> newUrlsOnly = List.of("https://example.com/new.jpg");

            DetailProcessBundle bundle = createBundleWithImages(allUrls);
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();
            DetailUpdateResult updateResult = new DetailUpdateResult(updatedProduct, allUrls);

            given(
                            crawledProductManager.updateFromDetailCrawlData(
                                    any(CrawledProduct.class), any(DetailCrawlData.class)))
                    .willReturn(updateResult);

            // 하나의 URL만 새로운 URL로 반환
            given(imageReadManager.filterNewImageUrls(any(CrawledProductId.class), anyList()))
                    .willReturn(newUrlsOnly);

            // When
            CrawledProduct result = facade.updateAndRequestUploadAndSync(existingProduct, bundle);

            // Then
            assertThat(result).isNotNull();

            // Factory에 새로운 URL 1개만 전달 검증
            verify(imageUploadOrchestrator, times(1))
                    .processImageUpload(imageUploadDataCaptor.capture());
            ImageUploadData capturedData = imageUploadDataCaptor.getValue();
            assertThat(capturedData.imageUrls()).hasSize(1);
            assertThat(capturedData.imageUrls()).containsExactly("https://example.com/new.jpg");

            // Sync 요청 검증
            verify(requestSyncUseCase, times(1)).requestIfReady(updatedProduct);
        }

        @Test
        @DisplayName("[성공] 모든 URL이 기존에 존재 → Factory 호출 안 함, Sync만 요청")
        void shouldSkipFactoryWhenAllUrlsExistButStillRequestSync() {
            // Given
            List<String> allUrls =
                    List.of(
                            "https://example.com/existing1.jpg",
                            "https://example.com/existing2.jpg");

            DetailProcessBundle bundle = createBundleWithImages(allUrls);
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();
            DetailUpdateResult updateResult = new DetailUpdateResult(updatedProduct, allUrls);

            given(
                            crawledProductManager.updateFromDetailCrawlData(
                                    any(CrawledProduct.class), any(DetailCrawlData.class)))
                    .willReturn(updateResult);

            // 빈 리스트 반환 (모든 URL이 이미 존재)
            given(imageReadManager.filterNewImageUrls(any(CrawledProductId.class), anyList()))
                    .willReturn(List.of());

            // When
            CrawledProduct result = facade.updateAndRequestUploadAndSync(existingProduct, bundle);

            // Then
            assertThat(result).isNotNull();

            // Factory 호출 안 함 검증
            verify(imageUploadOrchestrator, never()).processImageUpload(any());

            // Sync는 항상 요청
            verify(requestSyncUseCase, times(1)).requestIfReady(updatedProduct);
        }

        @Test
        @DisplayName("[성공] 이미지가 없는 경우 → Product 업데이트 + Sync 요청만 수행")
        void shouldOnlyUpdateProductAndRequestSyncWhenNoImages() {
            // Given
            DetailProcessBundle bundle = createBundleWithoutImages();
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();
            DetailUpdateResult updateResult = new DetailUpdateResult(updatedProduct, List.of());

            given(
                            crawledProductManager.updateFromDetailCrawlData(
                                    any(CrawledProduct.class), any(DetailCrawlData.class)))
                    .willReturn(updateResult);

            // When
            CrawledProduct result = facade.updateAndRequestUploadAndSync(existingProduct, bundle);

            // Then
            assertThat(result).isNotNull();

            // URL 필터링 호출 안 함 검증
            verify(imageReadManager, never())
                    .filterNewImageUrls(any(CrawledProductId.class), anyList());

            // Factory 호출 안 함 검증
            verify(imageUploadOrchestrator, never()).processImageUpload(any());

            // Sync는 항상 요청
            verify(requestSyncUseCase, times(1)).requestIfReady(updatedProduct);
        }
    }

    // === Helper Methods ===

    private DetailProcessBundle createBundleWithImages(List<String> imageUrls) {
        DetailCrawlData crawlData =
                DetailCrawlData.of(
                        ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"),
                        null,
                        "<p>Test Description</p>",
                        "ACTIVE",
                        "Korea",
                        "Seoul",
                        imageUrls,
                        FIXED_INSTANT);

        ImageUploadData imageUploadData = ImageUploadData.of(imageUrls, ImageType.DESCRIPTION);

        return new DetailProcessBundle(crawlData, imageUploadData);
    }

    private DetailProcessBundle createBundleWithoutImages() {
        DetailCrawlData crawlData =
                DetailCrawlData.of(
                        ProductCategory.of("100", "Women", "110", "Clothing", "111", "Dresses"),
                        null,
                        "<p>Test Description</p>",
                        "ACTIVE",
                        "Korea",
                        "Seoul",
                        List.of(),
                        FIXED_INSTANT);

        return new DetailProcessBundle(crawlData, null);
    }

    private CrawledProduct createMockProduct() {
        Instant now = FIXED_INSTANT;
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
