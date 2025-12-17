package com.ryuqq.crawlinghub.application.product.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.image.manager.ImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.application.product.dto.bundle.MiniShopProcessBundle;
import com.ryuqq.crawlinghub.application.product.manager.ImageOutboxManager;
import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
 * MiniShopProcessFacade 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MiniShopProcessFacade 테스트")
class MiniShopProcessFacadeTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductTransactionManager crawledProductManager;

    @Mock private ImageOutboxReadManager imageOutboxReadManager;

    @Mock private ImageOutboxManager imageOutboxManager;

    @Mock private TransactionEventRegistry eventRegistry;

    @Captor private ArgumentCaptor<List<CrawledProductImageOutbox>> outboxListCaptor;

    @Captor private ArgumentCaptor<ImageUploadRequestedEvent> eventCaptor;

    private MiniShopProcessFacade facade;

    @BeforeEach
    void setUp() {
        facade =
                new MiniShopProcessFacade(
                        crawledProductManager,
                        imageOutboxReadManager,
                        imageOutboxManager,
                        eventRegistry,
                        FIXED_CLOCK);
    }

    @Nested
    @DisplayName("createAndRequestImageUpload() 테스트")
    class CreateAndRequestImageUpload {

        @Test
        @DisplayName("[성공] 이미지가 있는 경우 → Product 생성 + Outbox 저장 + Event 등록")
        void shouldCreateProductAndSaveOutboxAndRegisterEventWhenImagesExist() {
            // Given
            List<String> imageUrls =
                    List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg");
            MiniShopProcessBundle bundle = createBundleWithImages(imageUrls);
            CrawledProduct createdProduct = createMockProduct();

            given(crawledProductManager.createFromMiniShopCrawlData(any(MiniShopCrawlData.class)))
                    .willReturn(createdProduct);

            // When
            CrawledProduct result = facade.createAndRequestImageUpload(bundle);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);

            // Outbox 저장 검증
            verify(imageOutboxManager, times(1)).persistAll(outboxListCaptor.capture());
            List<CrawledProductImageOutbox> savedOutboxes = outboxListCaptor.getValue();
            assertThat(savedOutboxes).hasSize(2);

            // Event 등록 검증
            verify(eventRegistry, times(1)).registerForPublish(eventCaptor.capture());
            ImageUploadRequestedEvent event = eventCaptor.getValue();
            assertThat(event.crawledProductId()).isEqualTo(PRODUCT_ID);
        }

        @Test
        @DisplayName("[성공] 이미지가 없는 경우 → Product 생성만 수행 (Outbox/Event 생략)")
        void shouldOnlyCreateProductWhenNoImages() {
            // Given
            MiniShopProcessBundle bundle = createBundleWithoutImages();
            CrawledProduct createdProduct = createMockProduct();

            given(crawledProductManager.createFromMiniShopCrawlData(any(MiniShopCrawlData.class)))
                    .willReturn(createdProduct);

            // When
            CrawledProduct result = facade.createAndRequestImageUpload(bundle);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);

            // Outbox 저장 안 함 검증
            verify(imageOutboxManager, never()).persistAll(anyList());

            // Event 등록 안 함 검증
            verify(eventRegistry, never()).registerForPublish(any());
        }
    }

    @Nested
    @DisplayName("updateAndRequestImageUpload() 테스트")
    class UpdateAndRequestImageUpload {

        @Test
        @DisplayName("[성공] 새로운 이미지가 있는 경우 → Product 업데이트 + Outbox 저장 + Event 등록")
        void shouldUpdateProductAndSaveOutboxAndRegisterEventWhenNewImagesExist() {
            // Given
            List<String> imageUrls =
                    List.of(
                            "https://example.com/new-image1.jpg",
                            "https://example.com/new-image2.jpg");
            MiniShopProcessBundle bundle = createBundleWithImages(imageUrls);
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();

            given(
                            crawledProductManager.updateFromMiniShop(
                                    any(CrawledProduct.class),
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                    any(Boolean.class)))
                    .willReturn(updatedProduct);

            // 모든 URL이 새로운 URL로 반환
            given(imageOutboxReadManager.filterNewImageUrls(any(CrawledProductId.class), anyList()))
                    .willReturn(imageUrls);

            // When
            CrawledProduct result = facade.updateAndRequestImageUpload(existingProduct, bundle);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(PRODUCT_ID);

            // Outbox 저장 검증
            verify(imageOutboxManager, times(1)).persistAll(outboxListCaptor.capture());
            List<CrawledProductImageOutbox> savedOutboxes = outboxListCaptor.getValue();
            assertThat(savedOutboxes).hasSize(2);

            // Event 등록 검증
            verify(eventRegistry, times(1)).registerForPublish(eventCaptor.capture());
            ImageUploadRequestedEvent event = eventCaptor.getValue();
            assertThat(event.crawledProductId()).isEqualTo(PRODUCT_ID);
        }

        @Test
        @DisplayName("[성공] 기존 URL 필터링 → 새로운 URL만 Outbox 저장")
        void shouldFilterExistingUrlsAndSaveOnlyNewUrls() {
            // Given
            List<String> allUrls =
                    List.of("https://example.com/existing.jpg", "https://example.com/new.jpg");
            List<String> newUrlsOnly = List.of("https://example.com/new.jpg");

            MiniShopProcessBundle bundle = createBundleWithImages(allUrls);
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();

            given(
                            crawledProductManager.updateFromMiniShop(
                                    any(CrawledProduct.class),
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                    any(Boolean.class)))
                    .willReturn(updatedProduct);

            // 하나의 URL만 새로운 URL로 반환 (existing.jpg는 이미 존재)
            given(imageOutboxReadManager.filterNewImageUrls(any(CrawledProductId.class), anyList()))
                    .willReturn(newUrlsOnly);

            // When
            CrawledProduct result = facade.updateAndRequestImageUpload(existingProduct, bundle);

            // Then
            assertThat(result).isNotNull();

            // Outbox는 새로운 URL 1개만 저장
            verify(imageOutboxManager, times(1)).persistAll(outboxListCaptor.capture());
            List<CrawledProductImageOutbox> savedOutboxes = outboxListCaptor.getValue();
            assertThat(savedOutboxes).hasSize(1);

            // Event 등록 검증
            verify(eventRegistry, times(1)).registerForPublish(any());
        }

        @Test
        @DisplayName("[성공] 모든 URL이 기존에 존재 → Outbox/Event 생략")
        void shouldSkipOutboxAndEventWhenAllUrlsExist() {
            // Given
            List<String> allUrls =
                    List.of(
                            "https://example.com/existing1.jpg",
                            "https://example.com/existing2.jpg");

            MiniShopProcessBundle bundle = createBundleWithImages(allUrls);
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();

            given(
                            crawledProductManager.updateFromMiniShop(
                                    any(CrawledProduct.class),
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                    any(Boolean.class)))
                    .willReturn(updatedProduct);

            // 빈 리스트 반환 (모든 URL이 이미 존재)
            given(imageOutboxReadManager.filterNewImageUrls(any(CrawledProductId.class), anyList()))
                    .willReturn(List.of());

            // When
            CrawledProduct result = facade.updateAndRequestImageUpload(existingProduct, bundle);

            // Then
            assertThat(result).isNotNull();

            // Outbox 저장 안 함 검증
            verify(imageOutboxManager, never()).persistAll(anyList());

            // Event 등록 안 함 검증
            verify(eventRegistry, never()).registerForPublish(any());
        }

        @Test
        @DisplayName("[성공] 이미지가 없는 경우 → Product 업데이트만 수행")
        void shouldOnlyUpdateProductWhenNoImages() {
            // Given
            MiniShopProcessBundle bundle = createBundleWithoutImages();
            CrawledProduct existingProduct = createMockProduct();
            CrawledProduct updatedProduct = createMockProduct();

            given(
                            crawledProductManager.updateFromMiniShop(
                                    any(CrawledProduct.class),
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                    any(Boolean.class)))
                    .willReturn(updatedProduct);

            // When
            CrawledProduct result = facade.updateAndRequestImageUpload(existingProduct, bundle);

            // Then
            assertThat(result).isNotNull();

            // URL 필터링 호출 안 함 검증
            verify(imageOutboxReadManager, never())
                    .filterNewImageUrls(any(CrawledProductId.class), anyList());

            // Outbox 저장 안 함 검증
            verify(imageOutboxManager, never()).persistAll(anyList());

            // Event 등록 안 함 검증
            verify(eventRegistry, never()).registerForPublish(any());
        }
    }

    // === Helper Methods ===

    private MiniShopProcessBundle createBundleWithImages(List<String> imageUrls) {
        MiniShopCrawlData crawlData =
                MiniShopCrawlData.of(
                        SELLER_ID,
                        ITEM_NO,
                        "Test Product",
                        "Test Brand",
                        ProductPrice.of(10000, 10000, 10000, 9000, 10, 10),
                        ProductImages.fromThumbnailUrls(imageUrls),
                        true,
                        FIXED_CLOCK.instant());

        ImageUploadData imageUploadData = ImageUploadData.of(imageUrls, ImageType.THUMBNAIL);

        return new MiniShopProcessBundle(crawlData, imageUploadData);
    }

    private MiniShopProcessBundle createBundleWithoutImages() {
        MiniShopCrawlData crawlData =
                MiniShopCrawlData.of(
                        SELLER_ID,
                        ITEM_NO,
                        "Test Product",
                        "Test Brand",
                        ProductPrice.of(10000, 10000, 10000, 9000, 10, 10),
                        ProductImages.empty(),
                        true,
                        FIXED_CLOCK.instant());

        ImageUploadData imageUploadData = ImageUploadData.of(List.of(), ImageType.THUMBNAIL);

        return new MiniShopProcessBundle(crawlData, imageUploadData);
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
