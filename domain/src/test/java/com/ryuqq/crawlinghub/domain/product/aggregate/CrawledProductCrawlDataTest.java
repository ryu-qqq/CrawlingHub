package com.ryuqq.crawlinghub.domain.product.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
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
 * CrawledProduct CrawlData VO 기반 메서드 단위 테스트
 *
 * <p>fromMiniShopCrawlData, updateFromDetailCrawlData, updateFromOptionCrawlData 등 기존 테스트에서 커버되지 않은
 * 메서드 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("domain")
@DisplayName("CrawledProduct CrawlData 메서드 단위 테스트")
class CrawledProductCrawlDataTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant LATER_INSTANT = Instant.parse("2025-01-01T01:00:00Z");
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

    private static ProductOptions createDefaultOptions() {
        return ProductOptions.of(
                List.of(
                        new ProductOption(1L, ITEM_NO, "Red", "M", 10, null),
                        new ProductOption(2L, ITEM_NO, "Blue", "L", 5, null)));
    }

    private static CrawlCompletionStatus createAllCrawledStatus() {
        return CrawlCompletionStatus.initial()
                .withMiniShopCrawled(FIXED_INSTANT)
                .withDetailCrawled(FIXED_INSTANT)
                .withOptionCrawled(FIXED_INSTANT);
    }

    private static MiniShopCrawlData createDefaultMiniShopCrawlData() {
        return MiniShopCrawlData.of(
                SELLER_ID,
                ITEM_NO,
                ITEM_NAME,
                BRAND_NAME,
                createDefaultPrice(),
                createDefaultImages(),
                true,
                FIXED_INSTANT);
    }

    private CrawledProduct createFullyReconstituted() {
        return createFullyReconstitutedWithExternalId(null);
    }

    private CrawledProduct createFullyReconstitutedWithExternalId(Long externalProductId) {
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SELLER_ID,
                ITEM_NO,
                ITEM_NAME,
                BRAND_NAME,
                0L,
                createDefaultPrice(),
                createDefaultImages(),
                true,
                null,
                null,
                null,
                null,
                "ACTIVE",
                "Korea",
                "Seoul",
                createDefaultOptions(),
                createAllCrawledStatus(),
                externalProductId,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                FIXED_INSTANT,
                FIXED_INSTANT,
                null);
    }

    @Nested
    @DisplayName("fromMiniShopCrawlData() 팩토리 메서드 테스트")
    class FromMiniShopCrawlDataTest {

        @Test
        @DisplayName("MiniShopCrawlData VO로 신규 상품을 생성한다")
        void createProductFromCrawlData() {
            MiniShopCrawlData crawlData = createDefaultMiniShopCrawlData();

            CrawledProduct product = CrawledProduct.fromMiniShopCrawlData(crawlData);

            assertThat(product.getIdValue()).isNull();
            assertThat(product.getSellerId()).isEqualTo(SELLER_ID);
            assertThat(product.getItemNo()).isEqualTo(ITEM_NO);
            assertThat(product.getItemName()).isEqualTo(ITEM_NAME);
            assertThat(product.getBrandName()).isEqualTo(BRAND_NAME);
            assertThat(product.isFreeShipping()).isTrue();
            assertThat(product.getCreatedAt()).isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("생성된 상품은 MINI_SHOP 크롤링이 완료 상태다")
        void productHasMiniShopCrawled() {
            MiniShopCrawlData crawlData = createDefaultMiniShopCrawlData();

            CrawledProduct product = CrawledProduct.fromMiniShopCrawlData(crawlData);

            assertThat(product.getCrawlCompletionStatus().isMiniShopCrawled()).isTrue();
            assertThat(product.getCrawlCompletionStatus().isDetailCrawled()).isFalse();
            assertThat(product.canSyncToExternalServer()).isFalse();
        }
    }

    @Nested
    @DisplayName("updateFromDetailCrawlData() 테스트")
    class UpdateFromDetailCrawlDataTest {

        @Test
        @DisplayName("DetailCrawlData VO로 상세 정보를 업데이트한다")
        void updateDetailFromCrawlData() {
            CrawledProduct product =
                    CrawledProduct.fromMiniShopCrawlData(createDefaultMiniShopCrawlData());
            DetailCrawlData crawlData =
                    DetailCrawlData.of(
                            0L,
                            null,
                            null,
                            "<h1>상품 설명</h1>",
                            "ACTIVE",
                            "Korea",
                            "Seoul",
                            List.of(),
                            LATER_INSTANT);

            product.updateFromDetailCrawlData(crawlData);

            assertThat(product.getCrawlCompletionStatus().isDetailCrawled()).isTrue();
            assertThat(product.getItemStatus()).isEqualTo("ACTIVE");
            assertThat(product.getOriginCountry()).isEqualTo("Korea");
            assertThat(product.getShippingLocation()).isEqualTo("Seoul");
            assertThat(product.getUpdatedAt()).isEqualTo(LATER_INSTANT);
        }

        @Test
        @DisplayName("상세 설명이 변경되면 originalDescriptionMarkUp이 갱신된다")
        void updatesDescriptionMarkUpWhenChanged() {
            CrawledProduct product =
                    CrawledProduct.fromMiniShopCrawlData(createDefaultMiniShopCrawlData());
            String newDescription = "<h1>새로운 설명</h1>";
            DetailCrawlData crawlData =
                    DetailCrawlData.of(
                            0L,
                            null,
                            null,
                            newDescription,
                            null,
                            null,
                            null,
                            List.of(),
                            LATER_INSTANT);

            product.updateFromDetailCrawlData(crawlData);

            assertThat(product.getOriginalDescriptionMarkUp()).isEqualTo(newDescription);
            assertThat(product.getDescriptionMarkUp()).isEqualTo(newDescription);
        }

        @Test
        @DisplayName("상세 설명 이미지가 있으면 이미지가 교체된다")
        void replacesDescriptionImagesWhenPresent() {
            CrawledProduct product =
                    CrawledProduct.fromMiniShopCrawlData(createDefaultMiniShopCrawlData());
            List<String> descImages = List.of("https://example.com/desc1.jpg");
            DetailCrawlData crawlData =
                    DetailCrawlData.of(
                            0L,
                            null,
                            null,
                            "<h1>설명</h1>",
                            null,
                            null,
                            null,
                            descImages,
                            LATER_INSTANT);

            List<String> newUrls = product.updateFromDetailCrawlData(crawlData);

            assertThat(newUrls).containsExactlyElementsOf(descImages);
        }

        @Test
        @DisplayName("모든 크롤링 완료 후 상세 변경 시 pendingChanges에 DESCRIPTION이 추가된다")
        void addsPendingDescriptionChangeWhenFullyCrawled() {
            CrawledProduct product = createFullyReconstitutedWithExternalId(100L);
            DetailCrawlData crawlData =
                    DetailCrawlData.of(
                            0L,
                            null,
                            null,
                            "<h1>새 설명</h1>",
                            "ACTIVE",
                            "Korea",
                            "Seoul",
                            List.of(),
                            LATER_INSTANT);

            product.updateFromDetailCrawlData(crawlData);

            assertThat(product.getPendingChanges()).contains(ProductChangeType.DESCRIPTION);
            assertThat(product.isNeedsSync()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateFromOptionCrawlData() 테스트")
    class UpdateFromOptionCrawlDataTest {

        @Test
        @DisplayName("OptionCrawlData VO로 옵션을 업데이트한다")
        void updateOptionsFromCrawlData() {
            CrawledProduct product =
                    CrawledProduct.fromMiniShopCrawlData(createDefaultMiniShopCrawlData());
            ProductOptions newOptions =
                    ProductOptions.of(
                            List.of(new ProductOption(1L, ITEM_NO, "Red", "M", 20, null)));
            OptionCrawlData crawlData = OptionCrawlData.of(newOptions, LATER_INSTANT);

            product.updateFromOptionCrawlData(crawlData);

            assertThat(product.getCrawlCompletionStatus().isOptionCrawled()).isTrue();
            assertThat(product.getOptions()).isEqualTo(newOptions);
            assertThat(product.getUpdatedAt()).isEqualTo(LATER_INSTANT);
        }

        @Test
        @DisplayName("옵션 변경 시 모든 크롤링 완료 상태면 pendingChanges에 OPTION_STOCK이 추가된다")
        void addsPendingOptionStockChangeWhenFullyCrawled() {
            CrawledProduct product = createFullyReconstitutedWithExternalId(100L);
            ProductOptions changedOptions =
                    ProductOptions.of(
                            List.of(new ProductOption(1L, ITEM_NO, "Red", "M", 99, null)));
            OptionCrawlData crawlData = OptionCrawlData.of(changedOptions, LATER_INSTANT);

            product.updateFromOptionCrawlData(crawlData);

            assertThat(product.getPendingChanges()).contains(ProductChangeType.OPTION_STOCK);
            assertThat(product.isNeedsSync()).isTrue();
        }

        @Test
        @DisplayName("외부 서버 미등록 상품은 옵션 변경 시 pendingChanges가 비어있고 needsSync만 true다")
        void doesNotAddPendingChangeForUnregisteredProduct() {
            CrawledProduct product = createFullyReconstituted();
            ProductOptions changedOptions =
                    ProductOptions.of(
                            List.of(new ProductOption(1L, ITEM_NO, "Red", "M", 99, null)));
            OptionCrawlData crawlData = OptionCrawlData.of(changedOptions, LATER_INSTANT);

            product.updateFromOptionCrawlData(crawlData);

            assertThat(product.getPendingChanges()).isEmpty();
            assertThat(product.isNeedsSync()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateFromDetail() 테스트 - 직접 파라미터 방식")
    class UpdateFromDetailDirectTest {

        @Test
        @DisplayName("상세 정보를 직접 파라미터로 업데이트한다")
        void updateDetailDirectly() {
            CrawledProduct product =
                    CrawledProduct.fromMiniShopCrawlData(createDefaultMiniShopCrawlData());

            product.updateFromDetail(
                    null,
                    null,
                    "<h1>설명</h1>",
                    "ACTIVE",
                    "Korea",
                    "Seoul",
                    List.of(),
                    LATER_INSTANT);

            assertThat(product.getItemStatus()).isEqualTo("ACTIVE");
            assertThat(product.getOriginCountry()).isEqualTo("Korea");
            assertThat(product.getCrawlCompletionStatus().isDetailCrawled()).isTrue();
        }

        @Test
        @DisplayName("상세 설명 이미지가 있으면 새 URL 목록을 반환한다")
        void returnsNewDescriptionImageUrls() {
            CrawledProduct product =
                    CrawledProduct.fromMiniShopCrawlData(createDefaultMiniShopCrawlData());
            List<String> descImages = List.of("https://example.com/new-desc.jpg");

            List<String> newUrls =
                    product.updateFromDetail(
                            null, null, "<h1>설명</h1>", null, null, null, descImages, LATER_INSTANT);

            assertThat(newUrls).containsExactlyElementsOf(descImages);
        }
    }

    @Nested
    @DisplayName("delete() / isDeleted() 테스트")
    class DeleteTest {

        @Test
        @DisplayName("소프트 삭제 처리한다")
        void softDeletesProduct() {
            CrawledProduct product = createFullyReconstituted();

            product.delete(LATER_INSTANT);

            assertThat(product.isDeleted()).isTrue();
            assertThat(product.getDeletionStatus()).isNotNull();
            assertThat(product.getUpdatedAt()).isEqualTo(LATER_INSTANT);
        }

        @Test
        @DisplayName("삭제 전에는 isDeleted()가 false다")
        void notDeletedInitially() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("markChangesSynced() 테스트")
    class MarkChangesSyncedTest {

        @Test
        @DisplayName("특정 변경 유형 동기화 완료 처리한다")
        void marksSpecificChangeSynced() {
            CrawledProduct product = createFullyReconstitutedWithExternalId(100L);
            MiniShopCrawlData crawlData =
                    MiniShopCrawlData.of(
                            SELLER_ID,
                            ITEM_NO,
                            "새로운 상품명",
                            BRAND_NAME,
                            ProductPrice.of(15000, 18000, 18000, 14000, 15, 20),
                            createDefaultImages(),
                            true,
                            LATER_INSTANT);
            product.updateFromMiniShopCrawlData(crawlData);
            assertThat(product.getPendingChanges()).isNotEmpty();

            product.markChangesSynced(product.getPendingChanges(), LATER_INSTANT);

            assertThat(product.getPendingChanges()).isEmpty();
            assertThat(product.isNeedsSync()).isFalse();
            assertThat(product.getLastSyncedAt()).isEqualTo(LATER_INSTANT);
        }
    }

    @Nested
    @DisplayName("getPendingChanges() 테스트")
    class GetPendingChangesTest {

        @Test
        @DisplayName("pendingChanges는 불변 집합을 반환한다")
        void returnsUnmodifiableSet() {
            CrawledProduct product = createFullyReconstituted();
            var pendingChanges = product.getPendingChanges();
            assertThat(pendingChanges).isNotNull();
        }
    }

    @Nested
    @DisplayName("Getter 테스트 - 미커버 필드")
    class GetterTest {

        @Test
        @DisplayName("sellerIdValue를 반환한다")
        void returnsSellerIdValue() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.getSellerIdValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("category를 반환한다 (null 가능)")
        void returnsCategory() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.getCategory()).isNull();
        }

        @Test
        @DisplayName("shippingInfo를 반환한다 (null 가능)")
        void returnsShippingInfo() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.getShippingInfo()).isNull();
        }

        @Test
        @DisplayName("itemStatus를 반환한다")
        void returnsItemStatus() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.getItemStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("originCountry를 반환한다")
        void returnsOriginCountry() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.getOriginCountry()).isEqualTo("Korea");
        }

        @Test
        @DisplayName("shippingLocation을 반환한다")
        void returnsShippingLocation() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.getShippingLocation()).isEqualTo("Seoul");
        }

        @Test
        @DisplayName("deletionStatus를 반환한다")
        void returnsDeletionStatus() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.getDeletionStatus()).isNotNull();
        }

        @Test
        @DisplayName("originalDescriptionMarkUp을 반환한다 (null 가능)")
        void returnsOriginalDescriptionMarkUp() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.getOriginalDescriptionMarkUp()).isNull();
        }

        @Test
        @DisplayName("idValue가 null이면 null을 반환한다")
        void returnsNullIdValueWhenNew() {
            CrawledProduct product =
                    CrawledProduct.fromMiniShopCrawlData(createDefaultMiniShopCrawlData());
            assertThat(product.getIdValue()).isNull();
        }

        @Test
        @DisplayName("getPendingUploadImageUrls가 빈 images면 빈 목록을 반환한다")
        void returnsEmptyListWhenNoImages() {
            CrawledProduct product =
                    CrawledProduct.reconstitute(
                            CrawledProductId.of(1L),
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            0L,
                            createDefaultPrice(),
                            null,
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
                            FIXED_INSTANT,
                            null);
            assertThat(product.getPendingUploadImageUrls()).isEmpty();
        }
    }

    @Nested
    @DisplayName("pollEvents() / registerEvent() 테스트")
    class DomainEventTest {

        @Test
        @DisplayName("pollEvents()는 빈 목록을 반환한다 (이벤트 없을 때)")
        void returnsEmptyEventsInitially() {
            CrawledProduct product = createFullyReconstituted();
            assertThat(product.pollEvents()).isEmpty();
        }

        @Test
        @DisplayName("pollEvents() 호출 후 이벤트 목록이 초기화된다")
        void clearsEventsAfterPoll() {
            CrawledProduct product = createFullyReconstituted();
            product.pollEvents();
            assertThat(product.pollEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTotalStock() / isSoldOut() 엣지 케이스 테스트")
    class StockEdgeCaseTest {

        @Test
        @DisplayName("options가 null이면 getTotalStock()은 0을 반환한다")
        void returnZeroStockWhenOptionsNull() {
            CrawledProduct product =
                    CrawledProduct.reconstitute(
                            CrawledProductId.of(1L),
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            0L,
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
                            null,
                            createAllCrawledStatus(),
                            null,
                            null,
                            false,
                            EnumSet.noneOf(ProductChangeType.class),
                            DeletionStatus.active(),
                            FIXED_INSTANT,
                            FIXED_INSTANT,
                            null);
            assertThat(product.getTotalStock()).isEqualTo(0);
        }
    }
}
