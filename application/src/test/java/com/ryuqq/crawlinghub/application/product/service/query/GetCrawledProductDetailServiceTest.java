package com.ryuqq.crawlinghub.application.product.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledProductAssembler;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.exception.CrawledProductNotFoundException;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
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
 * GetCrawledProductDetailService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCrawledProductDetailService 테스트")
class GetCrawledProductDetailServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 12345L;

    @Mock private CrawledProductReadManager productReadManager;

    @Mock private CrawledProductImageReadManager imageReadManager;

    @Mock private CrawledProductAssembler assembler;

    private GetCrawledProductDetailService service;

    @BeforeEach
    void setUp() {
        service =
                new GetCrawledProductDetailService(productReadManager, imageReadManager, assembler);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 상품 존재 시 → DetailResponse 반환 (업로드된 이미지 포함)")
        void shouldReturnDetailResponseWhenProductExists() {
            // Given
            CrawledProduct product = createMockProduct();
            List<CrawledProductImage> thumbnails = List.of(createMockImage(ImageType.THUMBNAIL));
            List<CrawledProductImage> descriptionImages =
                    List.of(createMockImage(ImageType.DESCRIPTION));
            CrawledProductDetailResponse expectedResponse = createMockDetailResponse();

            given(productReadManager.findById(any(CrawledProductId.class)))
                    .willReturn(Optional.of(product));
            given(
                            imageReadManager.findUploadedThumbnailsByCrawledProductId(
                                    any(CrawledProductId.class)))
                    .willReturn(thumbnails);
            given(
                            imageReadManager.findUploadedDescriptionImagesByCrawledProductId(
                                    any(CrawledProductId.class)))
                    .willReturn(descriptionImages);
            given(
                            assembler.toDetailResponseWithUploadedImages(
                                    product, thumbnails, descriptionImages))
                    .willReturn(expectedResponse);

            // When
            CrawledProductDetailResponse result = service.execute(PRODUCT_ID.value());

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.id()).isEqualTo(PRODUCT_ID.value());
            then(productReadManager).should().findById(PRODUCT_ID);
            then(imageReadManager).should().findUploadedThumbnailsByCrawledProductId(PRODUCT_ID);
            then(imageReadManager)
                    .should()
                    .findUploadedDescriptionImagesByCrawledProductId(PRODUCT_ID);
            then(assembler)
                    .should()
                    .toDetailResponseWithUploadedImages(product, thumbnails, descriptionImages);
        }

        @Test
        @DisplayName("[성공] 업로드된 이미지 없을 시 → 빈 이미지 목록으로 응답")
        void shouldReturnDetailResponseWithEmptyImagesWhenNoUploadedImages() {
            // Given
            CrawledProduct product = createMockProduct();
            List<CrawledProductImage> emptyThumbnails = List.of();
            List<CrawledProductImage> emptyDescriptionImages = List.of();
            CrawledProductDetailResponse expectedResponse = createMockDetailResponse();

            given(productReadManager.findById(any(CrawledProductId.class)))
                    .willReturn(Optional.of(product));
            given(
                            imageReadManager.findUploadedThumbnailsByCrawledProductId(
                                    any(CrawledProductId.class)))
                    .willReturn(emptyThumbnails);
            given(
                            imageReadManager.findUploadedDescriptionImagesByCrawledProductId(
                                    any(CrawledProductId.class)))
                    .willReturn(emptyDescriptionImages);
            given(
                            assembler.toDetailResponseWithUploadedImages(
                                    product, emptyThumbnails, emptyDescriptionImages))
                    .willReturn(expectedResponse);

            // When
            CrawledProductDetailResponse result = service.execute(PRODUCT_ID.value());

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(imageReadManager).should().findUploadedThumbnailsByCrawledProductId(PRODUCT_ID);
            then(imageReadManager)
                    .should()
                    .findUploadedDescriptionImagesByCrawledProductId(PRODUCT_ID);
        }

        @Test
        @DisplayName("[실패] 상품 미존재 시 → CrawledProductNotFoundException 발생")
        void shouldThrowExceptionWhenProductNotExists() {
            // Given
            given(productReadManager.findById(any(CrawledProductId.class)))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(PRODUCT_ID.value()))
                    .isInstanceOf(CrawledProductNotFoundException.class);

            then(productReadManager).should().findById(PRODUCT_ID);
            then(imageReadManager).should(never()).findUploadedThumbnailsByCrawledProductId(any());
            then(imageReadManager)
                    .should(never())
                    .findUploadedDescriptionImagesByCrawledProductId(any());
            then(assembler).should(never()).toDetailResponseWithUploadedImages(any(), any(), any());
        }
    }

    // === Helper Methods ===

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

    private CrawledProductImage createMockImage(ImageType imageType) {
        Instant now = FIXED_CLOCK.instant();
        return CrawledProductImage.reconstitute(
                1L,
                PRODUCT_ID,
                "https://example.com/image.jpg",
                imageType,
                0,
                "https://s3.example.com/image.jpg",
                "file-asset-123",
                now,
                now);
    }

    private CrawledProductDetailResponse createMockDetailResponse() {
        Instant now = FIXED_CLOCK.instant();
        return new CrawledProductDetailResponse(
                PRODUCT_ID.value(),
                SELLER_ID.value(),
                ITEM_NO,
                "Test Product",
                "Test Brand",
                "ACTIVE",
                "Korea",
                "Seoul",
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now);
    }
}
