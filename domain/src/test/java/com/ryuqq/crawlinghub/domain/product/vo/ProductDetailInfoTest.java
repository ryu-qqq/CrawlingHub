package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("ProductDetailInfo Value Object 단위 테스트")
class ProductDetailInfoTest {

    private ProductDetailInfo defaultInfo() {
        return ProductDetailInfo.builder()
                .sellerNo(100L)
                .sellerId("seller123")
                .itemNo(12345L)
                .itemName("테스트 상품")
                .brandName("나이키")
                .brandNameKr("나이키")
                .brandCode(1L)
                .category(ProductCategory.of("W", "여성", "001", "가방", "A01", "백팩"))
                .normalPrice(200000)
                .sellingPrice(150000)
                .discountPrice(130000)
                .discountRate(25)
                .stock(10)
                .isSoldOut(false)
                .shipping(ShippingInfo.freeShipping("DOMESTIC", 3))
                .bannerImages(List.of("https://img.com/banner.jpg"))
                .detailImages(List.of("https://img.com/detail.jpg"))
                .descriptionMarkUp("<p>상품 설명</p>")
                .originCountry("한국")
                .itemStatus("새상품")
                .build();
    }

    @Nested
    @DisplayName("생성 검증 테스트")
    class CreationValidationTest {

        @Test
        @DisplayName("sellerNo가 0 이하이면 예외가 발생한다")
        void invalidSellerNoThrowsException() {
            assertThatThrownBy(
                            () ->
                                    ProductDetailInfo.builder()
                                            .sellerNo(0L)
                                            .sellerId("seller")
                                            .itemNo(1L)
                                            .itemName("상품")
                                            .normalPrice(100)
                                            .sellingPrice(100)
                                            .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerNo");
        }

        @Test
        @DisplayName("sellerId가 null이면 예외가 발생한다")
        void nullSellerIdThrowsException() {
            assertThatThrownBy(
                            () ->
                                    ProductDetailInfo.builder()
                                            .sellerNo(1L)
                                            .sellerId(null)
                                            .itemNo(1L)
                                            .itemName("상품")
                                            .normalPrice(100)
                                            .sellingPrice(100)
                                            .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId");
        }

        @Test
        @DisplayName("itemNo가 0 이하이면 예외가 발생한다")
        void invalidItemNoThrowsException() {
            assertThatThrownBy(
                            () ->
                                    ProductDetailInfo.builder()
                                            .sellerNo(1L)
                                            .sellerId("seller")
                                            .itemNo(0L)
                                            .itemName("상품")
                                            .normalPrice(100)
                                            .sellingPrice(100)
                                            .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemNo");
        }

        @Test
        @DisplayName("itemName이 null이면 예외가 발생한다")
        void nullItemNameThrowsException() {
            assertThatThrownBy(
                            () ->
                                    ProductDetailInfo.builder()
                                            .sellerNo(1L)
                                            .sellerId("seller")
                                            .itemNo(1L)
                                            .itemName(null)
                                            .normalPrice(100)
                                            .sellingPrice(100)
                                            .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemName");
        }

        @Test
        @DisplayName("normalPrice가 음수이면 예외가 발생한다")
        void negativeNormalPriceThrowsException() {
            assertThatThrownBy(
                            () ->
                                    ProductDetailInfo.builder()
                                            .sellerNo(1L)
                                            .sellerId("seller")
                                            .itemNo(1L)
                                            .itemName("상품")
                                            .normalPrice(-1)
                                            .sellingPrice(100)
                                            .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("normalPrice");
        }

        @Test
        @DisplayName("discountRate가 100 초과이면 예외가 발생한다")
        void invalidDiscountRateThrowsException() {
            assertThatThrownBy(
                            () ->
                                    ProductDetailInfo.builder()
                                            .sellerNo(1L)
                                            .sellerId("seller")
                                            .itemNo(1L)
                                            .itemName("상품")
                                            .normalPrice(100)
                                            .sellingPrice(100)
                                            .discountRate(101)
                                            .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("discountRate");
        }

        @Test
        @DisplayName("stock이 음수이면 예외가 발생한다")
        void negativeStockThrowsException() {
            assertThatThrownBy(
                            () ->
                                    ProductDetailInfo.builder()
                                            .sellerNo(1L)
                                            .sellerId("seller")
                                            .itemNo(1L)
                                            .itemName("상품")
                                            .normalPrice(100)
                                            .sellingPrice(100)
                                            .stock(-1)
                                            .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("stock");
        }

        @Test
        @DisplayName("bannerImages가 null이면 빈 리스트로 방어적 복사된다")
        void nullBannerImagesBecomesEmptyList() {
            ProductDetailInfo info =
                    ProductDetailInfo.builder()
                            .sellerNo(1L)
                            .sellerId("seller")
                            .itemNo(1L)
                            .itemName("상품")
                            .normalPrice(100)
                            .sellingPrice(100)
                            .bannerImages(null)
                            .build();

            assertThat(info.bannerImages()).isEmpty();
        }

        @Test
        @DisplayName("descriptionMarkUp이 null이면 빈 문자열로 대체된다")
        void nullDescriptionMarkUpBecomesEmptyString() {
            ProductDetailInfo info =
                    ProductDetailInfo.builder()
                            .sellerNo(1L)
                            .sellerId("seller")
                            .itemNo(1L)
                            .itemName("상품")
                            .normalPrice(100)
                            .sellingPrice(100)
                            .descriptionMarkUp(null)
                            .build();

            assertThat(info.descriptionMarkUp()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder 테스트")
    class BuilderTest {

        @Test
        @DisplayName("빌더로 모든 필드를 설정하여 생성한다")
        void buildWithAllFields() {
            ProductDetailInfo info = defaultInfo();

            assertThat(info.sellerNo()).isEqualTo(100L);
            assertThat(info.sellerId()).isEqualTo("seller123");
            assertThat(info.itemNo()).isEqualTo(12345L);
            assertThat(info.itemName()).isEqualTo("테스트 상품");
            assertThat(info.brandName()).isEqualTo("나이키");
            assertThat(info.normalPrice()).isEqualTo(200000);
            assertThat(info.sellingPrice()).isEqualTo(150000);
            assertThat(info.discountPrice()).isEqualTo(130000);
            assertThat(info.discountRate()).isEqualTo(25);
            assertThat(info.stock()).isEqualTo(10);
            assertThat(info.isSoldOut()).isFalse();
        }

        @Test
        @DisplayName("bannerImages는 방어적 복사된다")
        void bannerImagesAreDefensivelyCopied() {
            List<String> mutableList = new ArrayList<>();
            mutableList.add("https://img.com/banner.jpg");

            ProductDetailInfo info =
                    ProductDetailInfo.builder()
                            .sellerNo(1L)
                            .sellerId("seller")
                            .itemNo(1L)
                            .itemName("상품")
                            .normalPrice(100)
                            .sellingPrice(100)
                            .bannerImages(mutableList)
                            .build();

            mutableList.add("https://img.com/banner2.jpg");

            assertThat(info.bannerImages()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAllImageUrls() 테스트")
    class GetAllImageUrlsTest {

        @Test
        @DisplayName("배너와 상세 이미지를 합쳐서 반환한다")
        void returnsCombinedImageUrls() {
            ProductDetailInfo info = defaultInfo();

            List<String> allUrls = info.getAllImageUrls();

            assertThat(allUrls).hasSize(2);
            assertThat(allUrls)
                    .contains("https://img.com/banner.jpg", "https://img.com/detail.jpg");
        }

        @Test
        @DisplayName("이미지가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoImages() {
            ProductDetailInfo info =
                    ProductDetailInfo.builder()
                            .sellerNo(1L)
                            .sellerId("seller")
                            .itemNo(1L)
                            .itemName("상품")
                            .normalPrice(100)
                            .sellingPrice(100)
                            .build();

            assertThat(info.getAllImageUrls()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getMainImageUrl() 테스트")
    class GetMainImageUrlTest {

        @Test
        @DisplayName("배너 이미지가 있으면 첫 번째 배너 이미지를 반환한다")
        void returnFirstBannerImage() {
            ProductDetailInfo info = defaultInfo();

            assertThat(info.getMainImageUrl()).isEqualTo("https://img.com/banner.jpg");
        }

        @Test
        @DisplayName("배너 이미지가 없으면 상세 이미지의 첫 번째를 반환한다")
        void returnsFirstDetailImageWhenNoBannerImage() {
            ProductDetailInfo info =
                    ProductDetailInfo.builder()
                            .sellerNo(1L)
                            .sellerId("seller")
                            .itemNo(1L)
                            .itemName("상품")
                            .normalPrice(100)
                            .sellingPrice(100)
                            .detailImages(List.of("https://img.com/detail.jpg"))
                            .build();

            assertThat(info.getMainImageUrl()).isEqualTo("https://img.com/detail.jpg");
        }

        @Test
        @DisplayName("이미지가 없으면 null을 반환한다")
        void returnsNullWhenNoImages() {
            ProductDetailInfo info =
                    ProductDetailInfo.builder()
                            .sellerNo(1L)
                            .sellerId("seller")
                            .itemNo(1L)
                            .itemName("상품")
                            .normalPrice(100)
                            .sellingPrice(100)
                            .build();

            assertThat(info.getMainImageUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("hasDiscount() 테스트")
    class HasDiscountTest {

        @Test
        @DisplayName("discountRate가 0보다 크면 할인 중이다")
        void positiveDiscountRateMeansDiscount() {
            ProductDetailInfo info = defaultInfo();
            assertThat(info.hasDiscount()).isTrue();
        }

        @Test
        @DisplayName("discountRate가 0이면 할인이 없다")
        void zeroDiscountRateMeansNoDiscount() {
            ProductDetailInfo info =
                    ProductDetailInfo.builder()
                            .sellerNo(1L)
                            .sellerId("seller")
                            .itemNo(1L)
                            .itemName("상품")
                            .normalPrice(100)
                            .sellingPrice(100)
                            .discountRate(0)
                            .build();

            assertThat(info.hasDiscount()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPriceChange() 테스트")
    class HasPriceChangeTest {

        @Test
        @DisplayName("가격이 같으면 변경 없음을 반환한다")
        void samePricesReturnNoChange() {
            ProductDetailInfo info1 = defaultInfo();
            ProductDetailInfo info2 = defaultInfo();

            assertThat(info1.hasPriceChange(info2)).isFalse();
        }

        @Test
        @DisplayName("가격이 다르면 변경 있음을 반환한다")
        void differentPricesReturnChange() {
            ProductDetailInfo info1 = defaultInfo();
            ProductDetailInfo info2 =
                    ProductDetailInfo.builder()
                            .sellerNo(100L)
                            .sellerId("seller123")
                            .itemNo(12345L)
                            .itemName("테스트 상품")
                            .normalPrice(300000)
                            .sellingPrice(250000)
                            .build();

            assertThat(info1.hasPriceChange(info2)).isTrue();
        }

        @Test
        @DisplayName("null이면 변경 있음을 반환한다")
        void nullReturnsChange() {
            ProductDetailInfo info = defaultInfo();
            assertThat(info.hasPriceChange(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("hasStockChange() 테스트")
    class HasStockChangeTest {

        @Test
        @DisplayName("재고가 같으면 변경 없음을 반환한다")
        void sameStockReturnsNoChange() {
            ProductDetailInfo info1 = defaultInfo();
            ProductDetailInfo info2 = defaultInfo();

            assertThat(info1.hasStockChange(info2)).isFalse();
        }

        @Test
        @DisplayName("재고가 다르면 변경 있음을 반환한다")
        void differentStockReturnsChange() {
            ProductDetailInfo info1 = defaultInfo();
            ProductDetailInfo info2 =
                    ProductDetailInfo.builder()
                            .sellerNo(100L)
                            .sellerId("seller123")
                            .itemNo(12345L)
                            .itemName("테스트 상품")
                            .normalPrice(200000)
                            .sellingPrice(150000)
                            .stock(5)
                            .build();

            assertThat(info1.hasStockChange(info2)).isTrue();
        }
    }

    @Nested
    @DisplayName("toProductPrice() 테스트")
    class ToProductPriceTest {

        @Test
        @DisplayName("ProductPrice VO로 변환한다")
        void convertsToProductPrice() {
            ProductDetailInfo info = defaultInfo();

            ProductPrice price = info.toProductPrice();

            assertThat(price).isNotNull();
            assertThat(price.price()).isEqualTo(info.sellingPrice());
            assertThat(price.originalPrice()).isEqualTo(info.normalPrice());
            assertThat(price.discountRate()).isEqualTo(info.discountRate());
        }
    }
}
