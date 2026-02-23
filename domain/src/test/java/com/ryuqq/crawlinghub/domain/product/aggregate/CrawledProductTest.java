package com.ryuqq.crawlinghub.domain.product.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImage;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawledProduct 도메인 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("domain")
@DisplayName("CrawledProduct 단위 테스트")
class CrawledProductTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final SellerId SELLER_ID = SellerId.of(1L);
    private static final long ITEM_NO = 123456L;
    private static final String ITEM_NAME = "테스트 상품";
    private static final String BRAND_NAME = "테스트 브랜드";
    private static final String THUMBNAIL_URL = "https://example.com/thumb.jpg";

    private static ProductPrice createDefaultPrice() {
        return ProductPrice.of(10000, 12000, 12000, 9000, 10, 15);
    }

    private static ProductImages createDefaultImages() {
        return ProductImages.fromThumbnailUrls(List.of(THUMBNAIL_URL));
    }

    private static CrawlCompletionStatus createAllCrawledStatus() {
        return CrawlCompletionStatus.initial()
                .withMiniShopCrawled(FIXED_INSTANT)
                .withDetailCrawled(FIXED_INSTANT)
                .withOptionCrawled(FIXED_INSTANT);
    }

    private static ProductOptions createDefaultOptions() {
        return ProductOptions.of(
                List.of(
                        new ProductOption(1L, ITEM_NO, "Red", "M", 10, null),
                        new ProductOption(2L, ITEM_NO, "Blue", "L", 5, null)));
    }

    private static ProductOptions createSoldOutOptions() {
        return ProductOptions.of(
                List.of(
                        new ProductOption(1L, ITEM_NO, "Red", "M", 0, null),
                        new ProductOption(2L, ITEM_NO, "Blue", "L", 0, null)));
    }

    @Nested
    @DisplayName("fromMiniShop() 테스트")
    class FromMiniShopTests {

        @Test
        @DisplayName("성공 - MINI_SHOP 데이터로 신규 상품 생성")
        void shouldCreateNewProductFromMiniShop() {
            // Given
            ProductPrice price = createDefaultPrice();
            ProductImages images = createDefaultImages();

            // When
            CrawledProduct product =
                    CrawledProduct.fromMiniShop(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            price,
                            images,
                            true,
                            FIXED_INSTANT);

            // Then
            assertThat(product.getId()).isNotNull();
            assertThat(product.getIdValue()).isNull(); // unassigned
            assertThat(product.getSellerId()).isEqualTo(SELLER_ID);
            assertThat(product.getItemNo()).isEqualTo(ITEM_NO);
            assertThat(product.getItemName()).isEqualTo(ITEM_NAME);
            assertThat(product.getBrandName()).isEqualTo(BRAND_NAME);
            assertThat(product.getPrice()).isEqualTo(price);
            assertThat(product.getImages()).isEqualTo(images);
            assertThat(product.isFreeShipping()).isTrue();
            assertThat(product.getCreatedAt()).isEqualTo(FIXED_INSTANT);
            assertThat(product.getUpdatedAt()).isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("성공 - 초기 상태에서 MINI_SHOP만 크롤링 완료")
        void shouldMarkMiniShopAsCrawled() {
            // When
            CrawledProduct product =
                    CrawledProduct.fromMiniShop(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            false,
                            FIXED_INSTANT);

            // Then
            CrawlCompletionStatus status = product.getCrawlCompletionStatus();
            assertThat(status.isMiniShopCrawled()).isTrue();
            assertThat(status.isDetailCrawled()).isFalse();
            assertThat(status.isOptionCrawled()).isFalse();
            assertThat(product.canSyncToExternalServer()).isFalse();
        }
    }

    @Nested
    @DisplayName("reconstitute() 테스트")
    class ReconstituteTests {

        @Test
        @DisplayName("성공 - 기존 데이터로 완전 복원")
        void shouldReconstituteWithAllData() {
            // Given
            CrawledProductId id = CrawledProductId.of(1L);
            ProductPrice price = createDefaultPrice();
            ProductImages images = createDefaultImages();
            CrawlCompletionStatus crawlStatus = createAllCrawledStatus();
            ProductOptions options = createDefaultOptions();

            // When
            CrawledProduct product =
                    CrawledProduct.reconstitute(
                            id,
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            price,
                            images,
                            true,
                            null, // category
                            null, // shippingInfo
                            null, // originalDescriptionMarkUp
                            null, // descriptionMarkUp
                            "ACTIVE",
                            "Korea",
                            "Seoul",
                            options,
                            crawlStatus,
                            100L, // externalProductId
                            FIXED_INSTANT, // lastSyncedAt
                            false, // needsSync
                            EnumSet.noneOf(ProductChangeType.class),
                            DeletionStatus.active(),
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            // Then
            assertThat(product.getId()).isEqualTo(id);
            assertThat(product.getIdValue()).isEqualTo(1L);
            assertThat(product.getExternalProductId()).isEqualTo(100L);
            assertThat(product.getLastSyncedAt()).isEqualTo(FIXED_INSTANT);
            assertThat(product.isNeedsSync()).isFalse();
            assertThat(product.getOptions()).isEqualTo(options);
        }
    }

    @Nested
    @DisplayName("updateFromMiniShopCrawlData() 테스트")
    class UpdateFromMiniShopCrawlDataTests {

        @Test
        @DisplayName("성공 - 가격 변경 시 needsSync 설정")
        void shouldSetNeedsSyncWhenPriceChanges() {
            // Given
            CrawledProduct product = createProductWithAllCrawled();
            ProductPrice newPrice = ProductPrice.of(15000, 18000, 18000, 14000, 15, 20);
            Instant laterInstant = Instant.parse("2025-01-01T01:00:00Z");
            MiniShopCrawlData crawlData =
                    MiniShopCrawlData.of(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            newPrice,
                            createDefaultImages(),
                            true,
                            laterInstant);

            // When
            product.updateFromMiniShopCrawlData(crawlData);

            // Then
            assertThat(product.getPrice()).isEqualTo(newPrice);
            assertThat(product.isNeedsSync()).isTrue();
            assertThat(product.getUpdatedAt()).isEqualTo(Instant.parse("2025-01-01T01:00:00Z"));
        }

        @Test
        @DisplayName("성공 - 이름 변경 시 needsSync 설정")
        void shouldSetNeedsSyncWhenNameChanges() {
            // Given
            CrawledProduct product = createProductWithAllCrawled();
            Instant laterInstant = Instant.parse("2025-01-01T01:00:00Z");
            MiniShopCrawlData crawlData =
                    MiniShopCrawlData.of(
                            SELLER_ID,
                            ITEM_NO,
                            "새로운 상품명",
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            laterInstant);

            // When
            product.updateFromMiniShopCrawlData(crawlData);

            // Then
            assertThat(product.getItemName()).isEqualTo("새로운 상품명");
            assertThat(product.isNeedsSync()).isTrue();
        }

        @Test
        @DisplayName("성공 - 변경 없으면 needsSync 유지")
        void shouldNotSetNeedsSyncWhenNoChanges() {
            // Given
            CrawledProduct product = createProductWithAllCrawledNoSync();
            Instant laterInstant = Instant.parse("2025-01-01T01:00:00Z");
            MiniShopCrawlData crawlData =
                    MiniShopCrawlData.of(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            laterInstant);

            // When
            product.updateFromMiniShopCrawlData(crawlData);

            // Then
            assertThat(product.isNeedsSync()).isFalse();
        }
    }

    @Nested
    @DisplayName("markImageAsUploaded() 테스트")
    class MarkImageAsUploadedTests {

        @Test
        @DisplayName("성공 - 이미지 S3 업로드 완료 처리")
        void shouldMarkImageAsUploaded() {
            // Given
            CrawledProduct product =
                    CrawledProduct.fromMiniShop(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            FIXED_INSTANT);
            String s3Url = "https://s3.amazonaws.com/bucket/image.jpg";
            Instant laterInstant = Instant.parse("2025-01-01T01:00:00Z");

            // When
            product.markImageAsUploaded(THUMBNAIL_URL, s3Url, laterInstant);

            // Then
            assertThat(product.getImages().getMainImageS3Url()).isEqualTo(s3Url);
            assertThat(product.getUpdatedAt()).isEqualTo(Instant.parse("2025-01-01T01:00:00Z"));
        }

        @Test
        @DisplayName("성공 - 상세 설명 내 URL도 함께 교체")
        void shouldReplaceDescriptionMarkUpUrl() {
            // Given
            String originalUrl = "https://example.com/desc-image.jpg";
            String s3Url = "https://s3.amazonaws.com/bucket/desc-image.jpg";
            CrawledProduct product =
                    CrawledProduct.reconstitute(
                            CrawledProductId.of(1L),
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            null,
                            null,
                            "<img src=\"" + originalUrl + "\">",
                            "<img src=\"" + originalUrl + "\">",
                            "ACTIVE",
                            null,
                            null,
                            ProductOptions.empty(),
                            createAllCrawledStatus(),
                            null,
                            null,
                            false,
                            EnumSet.noneOf(ProductChangeType.class),
                            DeletionStatus.active(),
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            // When
            product.markImageAsUploaded(originalUrl, s3Url, FIXED_INSTANT);

            // Then
            assertThat(product.getDescriptionMarkUp()).contains(s3Url);
            assertThat(product.getDescriptionMarkUp()).doesNotContain(originalUrl);
        }
    }

    @Nested
    @DisplayName("allImagesUploaded() 테스트")
    class AllImagesUploadedTests {

        @Test
        @DisplayName("성공 - 모든 이미지 업로드 완료")
        void shouldReturnTrueWhenAllUploaded() {
            // Given
            ProductImage uploadedImage =
                    ProductImage.thumbnail(THUMBNAIL_URL, 0)
                            .withS3Uploaded("https://s3.amazonaws.com/bucket/image.jpg");
            ProductImages uploadedImages = ProductImages.of(List.of(uploadedImage));

            CrawledProduct product =
                    CrawledProduct.reconstitute(
                            CrawledProductId.of(1L),
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            uploadedImages,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            ProductOptions.empty(),
                            createAllCrawledStatus(),
                            null,
                            null,
                            false,
                            EnumSet.noneOf(ProductChangeType.class),
                            DeletionStatus.active(),
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            // Then
            assertThat(product.allImagesUploaded()).isTrue();
        }

        @Test
        @DisplayName("실패 - 업로드 대기 이미지 존재")
        void shouldReturnFalseWhenPendingExists() {
            // Given
            CrawledProduct product =
                    CrawledProduct.fromMiniShop(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            FIXED_INSTANT);

            // Then
            assertThat(product.allImagesUploaded()).isFalse();
        }
    }

    @Nested
    @DisplayName("canSyncToExternalServer() 테스트")
    class CanSyncToExternalServerTests {

        @Test
        @DisplayName("성공 - 모든 크롤링 완료 시 동기화 가능")
        void shouldAllowSyncWhenAllCrawled() {
            // Given
            CrawledProduct product = createProductWithAllCrawled();

            // Then
            assertThat(product.canSyncToExternalServer()).isTrue();
        }

        @Test
        @DisplayName("실패 - DETAIL 크롤링 미완료")
        void shouldNotAllowSyncWhenDetailNotCrawled() {
            // Given
            CrawledProduct product =
                    CrawledProduct.fromMiniShop(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            FIXED_INSTANT);

            // Then
            assertThat(product.canSyncToExternalServer()).isFalse();
        }
    }

    @Nested
    @DisplayName("needsExternalSync() 테스트")
    class NeedsExternalSyncTests {

        @Test
        @DisplayName("성공 - needsSync=true이고 모든 크롤링 완료 시 true")
        void shouldReturnTrueWhenNeedsSyncAndCanSync() {
            // Given
            CrawledProduct product = createProductWithAllCrawled();

            // Then
            assertThat(product.needsExternalSync()).isTrue();
        }

        @Test
        @DisplayName("실패 - needsSync=false면 false")
        void shouldReturnFalseWhenNotNeedsSync() {
            // Given
            CrawledProduct product = createProductWithAllCrawledNoSync();

            // Then
            assertThat(product.needsExternalSync()).isFalse();
        }

        @Test
        @DisplayName("실패 - needsSync=true여도 크롤링 미완료면 false")
        void shouldReturnFalseWhenCrawlNotComplete() {
            // Given - MINI_SHOP만 크롤링된 상품
            CrawledProduct product =
                    CrawledProduct.fromMiniShop(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            FIXED_INSTANT);

            // Then
            assertThat(product.needsExternalSync()).isFalse();
        }
    }

    @Nested
    @DisplayName("markAsSynced() 테스트")
    class MarkAsSyncedTests {

        @Test
        @DisplayName("성공 - 동기화 완료 처리")
        void shouldMarkAsSynced() {
            // Given
            CrawledProduct product = createProductWithAllCrawled();
            Instant laterInstant = Instant.parse("2025-01-01T02:00:00Z");

            // When
            product.markAsSynced(100L, laterInstant);

            // Then
            assertThat(product.getExternalProductId()).isEqualTo(100L);
            assertThat(product.getLastSyncedAt()).isEqualTo(Instant.parse("2025-01-01T02:00:00Z"));
            assertThat(product.isNeedsSync()).isFalse();
            assertThat(product.isRegisteredToExternalServer()).isTrue();
        }
    }

    @Nested
    @DisplayName("markSyncFailed() 테스트")
    class MarkSyncFailedTests {

        @Test
        @DisplayName("성공 - 동기화 실패 처리 (needsSync 유지)")
        void shouldMarkSyncFailed() {
            // Given
            CrawledProduct product = createProductWithAllCrawled();
            Instant laterInstant = Instant.parse("2025-01-01T02:00:00Z");

            // When
            product.markSyncFailed(laterInstant);

            // Then
            assertThat(product.isNeedsSync()).isTrue(); // 재시도 위해 유지
            assertThat(product.getUpdatedAt()).isEqualTo(Instant.parse("2025-01-01T02:00:00Z"));
        }
    }

    @Nested
    @DisplayName("isRegisteredToExternalServer() 테스트")
    class IsRegisteredToExternalServerTests {

        @Test
        @DisplayName("성공 - 외부 상품 ID 있으면 true")
        void shouldReturnTrueWhenHasExternalId() {
            // Given
            CrawledProduct product = createProductWithAllCrawled();
            product.markAsSynced(100L, FIXED_INSTANT);

            // Then
            assertThat(product.isRegisteredToExternalServer()).isTrue();
        }

        @Test
        @DisplayName("실패 - 외부 상품 ID 없으면 false")
        void shouldReturnFalseWhenNoExternalId() {
            // Given
            CrawledProduct product = createProductWithAllCrawled();

            // Then
            assertThat(product.isRegisteredToExternalServer()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSoldOut() / getTotalStock() 테스트")
    class StockTests {

        @Test
        @DisplayName("성공 - 모든 옵션 품절 시 isSoldOut() true")
        void shouldReturnTrueWhenAllSoldOut() {
            // Given
            CrawledProduct product =
                    CrawledProduct.reconstitute(
                            CrawledProductId.of(1L),
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            createSoldOutOptions(),
                            createAllCrawledStatus(),
                            null,
                            null,
                            false,
                            EnumSet.noneOf(ProductChangeType.class),
                            DeletionStatus.active(),
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            // Then
            assertThat(product.isSoldOut()).isTrue();
            assertThat(product.getTotalStock()).isZero();
        }

        @Test
        @DisplayName("성공 - 재고 있으면 isSoldOut() false")
        void shouldReturnFalseWhenHasStock() {
            // Given
            CrawledProduct product =
                    CrawledProduct.reconstitute(
                            CrawledProductId.of(1L),
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            createDefaultOptions(),
                            createAllCrawledStatus(),
                            null,
                            null,
                            false,
                            EnumSet.noneOf(ProductChangeType.class),
                            DeletionStatus.active(),
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            // Then
            assertThat(product.isSoldOut()).isFalse();
            assertThat(product.getTotalStock()).isEqualTo(15); // 10 + 5
        }

        @Test
        @DisplayName("성공 - 옵션 없으면 getTotalStock() 0")
        void shouldReturnZeroWhenNoOptions() {
            // Given
            CrawledProduct product =
                    CrawledProduct.fromMiniShop(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            FIXED_INSTANT);

            // Then
            assertThat(product.getTotalStock()).isZero();
        }
    }

    @Nested
    @DisplayName("updateFromOption() 테스트")
    class UpdateFromOptionTests {

        @Test
        @DisplayName("성공 - 옵션 업데이트 및 needsSync 설정")
        void shouldUpdateOptionsAndSetNeedsSync() {
            // Given
            CrawledProduct product = createProductWithAllCrawledNoSync();
            ProductOptions newOptions =
                    ProductOptions.of(
                            List.of(
                                    new ProductOption(1L, ITEM_NO, "Red", "M", 20, null), // 재고 변경
                                    new ProductOption(2L, ITEM_NO, "Blue", "L", 5, null)));
            Instant laterInstant = Instant.parse("2025-01-01T01:00:00Z");

            // When
            product.updateFromOption(newOptions, laterInstant);

            // Then
            assertThat(product.getOptions()).isEqualTo(newOptions);
            assertThat(product.getTotalStock()).isEqualTo(25);
            assertThat(product.getCrawlCompletionStatus().isOptionCrawled()).isTrue();
        }
    }

    @Nested
    @DisplayName("getPendingUploadImageUrls() 테스트")
    class GetPendingUploadImageUrlsTests {

        @Test
        @DisplayName("성공 - 업로드 대기 이미지 URL 목록 반환")
        void shouldReturnPendingUploadUrls() {
            // Given
            CrawledProduct product =
                    CrawledProduct.fromMiniShop(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            createDefaultImages(),
                            true,
                            FIXED_INSTANT);

            // When
            List<String> pendingUrls = product.getPendingUploadImageUrls();

            // Then
            assertThat(pendingUrls).containsExactly(THUMBNAIL_URL);
        }

        @Test
        @DisplayName("성공 - 모든 이미지 업로드 완료 시 빈 목록")
        void shouldReturnEmptyWhenAllUploaded() {
            // Given
            ProductImage uploadedImage =
                    ProductImage.thumbnail(THUMBNAIL_URL, 0)
                            .withS3Uploaded("https://s3.amazonaws.com/bucket/image.jpg");
            ProductImages uploadedImages = ProductImages.of(List.of(uploadedImage));

            CrawledProduct product =
                    CrawledProduct.reconstitute(
                            CrawledProductId.of(1L),
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            createDefaultPrice(),
                            uploadedImages,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            ProductOptions.empty(),
                            createAllCrawledStatus(),
                            null,
                            null,
                            false,
                            EnumSet.noneOf(ProductChangeType.class),
                            DeletionStatus.active(),
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            // When
            List<String> pendingUrls = product.getPendingUploadImageUrls();

            // Then
            assertThat(pendingUrls).isEmpty();
        }
    }

    // === 헬퍼 메서드 ===

    private CrawledProduct createProductWithAllCrawled() {
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SELLER_ID,
                ITEM_NO,
                ITEM_NAME,
                BRAND_NAME,
                createDefaultPrice(),
                createDefaultImages(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                createDefaultOptions(),
                createAllCrawledStatus(),
                null,
                null,
                true, // needsSync = true
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                FIXED_INSTANT,
                FIXED_INSTANT);
    }

    private CrawledProduct createProductWithAllCrawledNoSync() {
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SELLER_ID,
                ITEM_NO,
                ITEM_NAME,
                BRAND_NAME,
                createDefaultPrice(),
                createDefaultImages(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                createDefaultOptions(),
                createAllCrawledStatus(),
                null,
                null,
                false, // needsSync = false
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                FIXED_INSTANT,
                FIXED_INSTANT);
    }
}
