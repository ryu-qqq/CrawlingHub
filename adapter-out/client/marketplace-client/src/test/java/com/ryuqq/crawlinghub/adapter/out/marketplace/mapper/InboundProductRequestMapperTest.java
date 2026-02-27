package com.ryuqq.crawlinghub.adapter.out.marketplace.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateDescriptionRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateImagesRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdatePriceRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateProductsRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.mapper.InboundProductRequestMapper.ResolvedPrice;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductSyncOutboxId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImage;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("InboundProductRequestMapper 테스트")
class InboundProductRequestMapperTest {

    private final InboundProductRequestMapper mapper = new InboundProductRequestMapper();

    // --- 공통 헬퍼 ---

    private Seller createSellerWithOmsSellerId(Long omsSellerId) {
        return Seller.reconstitute(
                SellerId.of(200L),
                MustItSellerName.of("mustit-seller"),
                SellerName.of("test-seller"),
                omsSellerId,
                SellerStatus.ACTIVE,
                0,
                Instant.now(),
                Instant.now());
    }

    private CrawledProductSyncOutbox createOutbox() {
        return CrawledProductSyncOutbox.reconstitute(
                CrawledProductSyncOutboxId.of(1L),
                CrawledProductId.of(100L),
                SellerId.of(200L),
                12345L,
                CrawledProductSyncOutbox.SyncType.CREATE,
                "test-idem-key",
                null,
                ProductOutboxStatus.PENDING,
                0,
                null,
                Instant.now(),
                null);
    }

    private CrawledProduct createProductWithAll() {
        ProductPrice price = new ProductPrice(8000, 0, 10000, 8000, 20, 20);
        ProductImages images =
                ProductImages.of(
                        List.of(
                                ProductImage.thumbnail("https://img.test/thumb1.jpg", 0),
                                ProductImage.thumbnail("https://img.test/thumb2.jpg", 1),
                                ProductImage.description("https://img.test/detail1.jpg", 0)));
        ProductOptions options =
                ProductOptions.of(
                        List.of(
                                new ProductOption(1001L, 12345L, "블랙", "M", 10, ""),
                                new ProductOption(1002L, 12345L, "블랙", "L", 5, ""),
                                new ProductOption(1003L, 12345L, "화이트", "M", 3, "")));
        ProductCategory category = ProductCategory.of("H001", "헤더", "L001", "대분류", "M001", "중분류");

        return CrawledProduct.reconstitute(
                CrawledProductId.of(100L),
                SellerId.of(200L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                price,
                images,
                false,
                category,
                new ShippingInfo("EXPRESS", 3000, "PAID", 3, false),
                "<p>원본 설명</p>",
                "<p>가공 설명</p>",
                "SALE",
                "KR",
                "SEOUL",
                options,
                CrawlCompletionStatus.initial(),
                null,
                null,
                true,
                null,
                DeletionStatus.active(),
                Instant.now(),
                Instant.now());
    }

    private CrawledProduct createProductMinimal() {
        return CrawledProduct.fromMiniShop(
                SellerId.of(200L),
                12345L,
                "테스트 상품명",
                "테스트 브랜드",
                new ProductPrice(10000, 0, 12000, 0, 0, 0),
                null,
                false,
                Instant.now());
    }

    // ================================================================
    // toReceiveRequest
    // ================================================================

    @Nested
    @DisplayName("toReceiveRequest 테스트")
    class ToReceiveRequestTest {

        @Test
        @DisplayName("정상적인 입력으로 ReceiveInboundProductRequest를 생성한다")
        void toReceiveRequest_withValidInput_returnsRequest() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox();
            CrawledProduct product = createProductWithAll();
            Seller seller = createSellerWithOmsSellerId(999L);

            // when
            ReceiveInboundProductRequest request = mapper.toReceiveRequest(outbox, product, seller);

            // then
            assertThat(request.inboundSourceId()).isEqualTo(1L);
            assertThat(request.externalProductCode()).isEqualTo("12345");
            assertThat(request.productName()).isEqualTo("테스트 상품");
            assertThat(request.externalBrandCode()).isEqualTo("테스트 브랜드");
            assertThat(request.externalCategoryCode()).isEqualTo("H001M001");
            assertThat(request.sellerId()).isEqualTo(999L);
            assertThat(request.optionType()).isEqualTo("COMBINATION");
        }

        @Test
        @DisplayName("이미지가 올바르게 변환된다")
        void toReceiveRequest_convertsImagesCorrectly() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox();
            CrawledProduct product = createProductWithAll();
            Seller seller = createSellerWithOmsSellerId(999L);

            // when
            ReceiveInboundProductRequest request = mapper.toReceiveRequest(outbox, product, seller);

            // then
            assertThat(request.images()).hasSize(3);
            assertThat(request.images().get(0).imageType()).isEqualTo("THUMBNAIL");
            assertThat(request.images().get(2).imageType()).isEqualTo("DETAIL");
        }

        @Test
        @DisplayName("옵션 그룹이 색상/사이즈로 올바르게 생성된다")
        void toReceiveRequest_createsOptionGroups() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox();
            CrawledProduct product = createProductWithAll();
            Seller seller = createSellerWithOmsSellerId(999L);

            // when
            ReceiveInboundProductRequest request = mapper.toReceiveRequest(outbox, product, seller);

            // then
            assertThat(request.optionGroups()).hasSize(2);
            assertThat(request.optionGroups().get(0).optionGroupName()).isEqualTo("색상");
            assertThat(request.optionGroups().get(1).optionGroupName()).isEqualTo("사이즈");

            // 색상: 블랙, 화이트 (distinct)
            assertThat(request.optionGroups().get(0).optionValues()).hasSize(2);
            // 사이즈: M, L (distinct)
            assertThat(request.optionGroups().get(1).optionValues()).hasSize(2);
        }

        @Test
        @DisplayName("상품(Product) 목록이 옵션 수만큼 생성된다")
        void toReceiveRequest_createsProductsPerOption() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox();
            CrawledProduct product = createProductWithAll();
            Seller seller = createSellerWithOmsSellerId(999L);

            // when
            ReceiveInboundProductRequest request = mapper.toReceiveRequest(outbox, product, seller);

            // then
            assertThat(request.products()).hasSize(3);
            assertThat(request.products().get(0).skuCode()).isEqualTo("1001");
            assertThat(request.products().get(0).stockQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("omsSellerId가 null이면 IllegalStateException이 발생한다")
        void toReceiveRequest_withNullOmsSellerId_throwsException() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox();
            CrawledProduct product = createProductWithAll();
            Seller seller = createSellerWithOmsSellerId(null);

            // when & then
            assertThatThrownBy(() -> mapper.toReceiveRequest(outbox, product, seller))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("omsSellerId가 설정되지 않은 셀러");
        }

        @Test
        @DisplayName("설명이 DescriptionRequest에 올바르게 매핑된다")
        void toReceiveRequest_mapsDescription() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox();
            CrawledProduct product = createProductWithAll();
            Seller seller = createSellerWithOmsSellerId(999L);

            // when
            ReceiveInboundProductRequest request = mapper.toReceiveRequest(outbox, product, seller);

            // then
            assertThat(request.description()).isNotNull();
            assertThat(request.description().content()).isEqualTo("<p>가공 설명</p>");
        }

        @Test
        @DisplayName("카테고리가 null이면 빈 문자열이 반환된다")
        void toReceiveRequest_withNullCategory_returnsEmptyString() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox();
            CrawledProduct product = createProductMinimal();
            Seller seller = createSellerWithOmsSellerId(999L);

            // when
            ReceiveInboundProductRequest request = mapper.toReceiveRequest(outbox, product, seller);

            // then
            assertThat(request.externalCategoryCode()).isEmpty();
        }
    }

    // ================================================================
    // toUpdatePriceRequest
    // ================================================================

    @Nested
    @DisplayName("toUpdatePriceRequest 테스트")
    class ToUpdatePriceRequestTest {

        @Test
        @DisplayName("가격이 올바르게 변환된다")
        void toUpdatePriceRequest_convertsPrice() {
            // given
            CrawledProduct product = createProductWithAll();

            // when
            UpdatePriceRequest request = mapper.toUpdatePriceRequest(product);

            // then
            assertThat(request.regularPrice()).isEqualTo(10000);
            assertThat(request.currentPrice()).isEqualTo(8000);
        }
    }

    // ================================================================
    // toUpdateImagesRequest
    // ================================================================

    @Nested
    @DisplayName("toUpdateImagesRequest 테스트")
    class ToUpdateImagesRequestTest {

        @Test
        @DisplayName("이미지가 THUMBNAIL/DETAIL 타입으로 변환된다")
        void toUpdateImagesRequest_convertsImages() {
            // given
            CrawledProduct product = createProductWithAll();

            // when
            UpdateImagesRequest request = mapper.toUpdateImagesRequest(product);

            // then
            assertThat(request.images()).hasSize(3);
            long thumbnailCount =
                    request.images().stream()
                            .filter(img -> "THUMBNAIL".equals(img.imageType()))
                            .count();
            long detailCount =
                    request.images().stream()
                            .filter(img -> "DETAIL".equals(img.imageType()))
                            .count();
            assertThat(thumbnailCount).isEqualTo(2);
            assertThat(detailCount).isEqualTo(1);
        }

        @Test
        @DisplayName("이미지가 순서대로 sortOrder가 부여된다")
        void toUpdateImagesRequest_assignsSortOrder() {
            // given
            CrawledProduct product = createProductWithAll();

            // when
            UpdateImagesRequest request = mapper.toUpdateImagesRequest(product);

            // then
            for (int i = 0; i < request.images().size(); i++) {
                assertThat(request.images().get(i).sortOrder()).isEqualTo(i);
            }
        }

        @Test
        @DisplayName("이미지가 null이면 빈 리스트를 반환한다")
        void toUpdateImagesRequest_withNullImages_returnsEmpty() {
            // given
            CrawledProduct product = createProductMinimal();

            // when
            UpdateImagesRequest request = mapper.toUpdateImagesRequest(product);

            // then
            assertThat(request.images()).isEmpty();
        }
    }

    // ================================================================
    // toUpdateDescriptionRequest
    // ================================================================

    @Nested
    @DisplayName("toUpdateDescriptionRequest 테스트")
    class ToUpdateDescriptionRequestTest {

        @Test
        @DisplayName("설명 마크업이 올바르게 반환된다")
        void toUpdateDescriptionRequest_returnsContent() {
            // given
            CrawledProduct product = createProductWithAll();

            // when
            UpdateDescriptionRequest request = mapper.toUpdateDescriptionRequest(product);

            // then
            assertThat(request.content()).isEqualTo("<p>가공 설명</p>");
        }
    }

    // ================================================================
    // toUpdateProductsRequest
    // ================================================================

    @Nested
    @DisplayName("toUpdateProductsRequest 테스트")
    class ToUpdateProductsRequestTest {

        @Test
        @DisplayName("옵션 그룹과 상품 데이터가 올바르게 생성된다")
        void toUpdateProductsRequest_createsGroupsAndProducts() {
            // given
            CrawledProduct product = createProductWithAll();

            // when
            UpdateProductsRequest request = mapper.toUpdateProductsRequest(product);

            // then
            assertThat(request.optionGroups()).hasSize(2);
            assertThat(request.products()).hasSize(3);
        }

        @Test
        @DisplayName("옵션이 없으면 빈 리스트를 반환한다")
        void toUpdateProductsRequest_withNoOptions_returnsEmpty() {
            // given
            CrawledProduct product = createProductMinimal();

            // when
            UpdateProductsRequest request = mapper.toUpdateProductsRequest(product);

            // then
            assertThat(request.optionGroups()).isEmpty();
            assertThat(request.products()).isEmpty();
        }

        @Test
        @DisplayName("상품 데이터에 selectedOptions가 올바르게 매핑된다")
        void toUpdateProductsRequest_mapsSelectedOptions() {
            // given
            CrawledProduct product = createProductWithAll();

            // when
            UpdateProductsRequest request = mapper.toUpdateProductsRequest(product);

            // then
            UpdateProductsRequest.ProductDataRequest first = request.products().get(0);
            assertThat(first.selectedOptions()).hasSize(2);
            assertThat(first.selectedOptions().get(0).optionGroupName()).isEqualTo("색상");
            assertThat(first.selectedOptions().get(1).optionGroupName()).isEqualTo("사이즈");
        }
    }

    // ================================================================
    // getInboundSourceId / getExternalProductCode
    // ================================================================

    @Nested
    @DisplayName("유틸 메서드 테스트")
    class UtilMethodsTest {

        @Test
        @DisplayName("getInboundSourceId는 1을 반환한다")
        void getInboundSourceId_returnsOne() {
            assertThat(mapper.getInboundSourceId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getExternalProductCode는 outbox의 itemNo를 문자열로 반환한다")
        void getExternalProductCode_returnsItemNoAsString() {
            // given
            CrawledProductSyncOutbox outbox = createOutbox();

            // when
            String code = mapper.getExternalProductCode(outbox);

            // then
            assertThat(code).isEqualTo("12345");
        }
    }

    // ================================================================
    // resolvePrice (package-private)
    // ================================================================

    @Nested
    @DisplayName("resolvePrice 테스트")
    class ResolvePriceTest {

        @Test
        @DisplayName("null 가격이면 (0, 0)을 반환한다")
        void resolvePrice_withNull_returnsZero() {
            ResolvedPrice result = mapper.resolvePrice(null);
            assertThat(result.regularPrice()).isZero();
            assertThat(result.currentPrice()).isZero();
        }

        @Test
        @DisplayName("appPrice만 있으면 (appPrice, appPrice)를 반환한다")
        void resolvePrice_onlyAppPrice_returnsSameForBoth() {
            // discountPrice > 0, normalPrice == 0, sellingPrice == 0
            ProductPrice price = new ProductPrice(0, 0, 0, 5000, 0, 0);
            ResolvedPrice result = mapper.resolvePrice(price);
            assertThat(result.regularPrice()).isEqualTo(5000);
            assertThat(result.currentPrice()).isEqualTo(5000);
        }

        @Test
        @DisplayName("appPrice + normalPrice가 있으면 (normalPrice, appPrice)를 반환한다")
        void resolvePrice_appPriceAndNormalPrice_returnsNormalAndApp() {
            // discountPrice > 0, normalPrice > 0, sellingPrice == 0
            ProductPrice price = new ProductPrice(0, 0, 10000, 7000, 0, 0);
            ResolvedPrice result = mapper.resolvePrice(price);
            assertThat(result.regularPrice()).isEqualTo(10000);
            assertThat(result.currentPrice()).isEqualTo(7000);
        }

        @Test
        @DisplayName("appPrice + sellingPrice가 있으면 (sellingPrice, sellingPrice)를 반환한다")
        void resolvePrice_appPriceAndSellingPrice_returnsSellingForBoth() {
            // discountPrice > 0, normalPrice == 0, sellingPrice > 0
            ProductPrice price = new ProductPrice(8000, 0, 0, 7000, 0, 0);
            ResolvedPrice result = mapper.resolvePrice(price);
            assertThat(result.regularPrice()).isEqualTo(8000);
            assertThat(result.currentPrice()).isEqualTo(8000);
        }

        @Test
        @DisplayName("모든 가격이 있으면 (normalPrice, sellingPrice)를 반환한다")
        void resolvePrice_allPricesPresent_returnsNormalAndSelling() {
            // normalPrice > 0, sellingPrice > 0, discountPrice > 0
            ProductPrice price = new ProductPrice(8000, 0, 10000, 7000, 0, 0);
            ResolvedPrice result = mapper.resolvePrice(price);
            assertThat(result.regularPrice()).isEqualTo(10000);
            assertThat(result.currentPrice()).isEqualTo(8000);
        }

        @Test
        @DisplayName("모든 가격이 0이면 (0, 0)을 반환한다")
        void resolvePrice_allZero_returnsZero() {
            ProductPrice price = new ProductPrice(0, 0, 0, 0, 0, 0);
            ResolvedPrice result = mapper.resolvePrice(price);
            assertThat(result.regularPrice()).isZero();
            assertThat(result.currentPrice()).isZero();
        }
    }
}
