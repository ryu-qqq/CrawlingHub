package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("MiniShopCrawlData Value Object 단위 테스트")
class MiniShopCrawlDataTest {

    private static final SellerId SELLER_ID = SellerId.of(1L);
    private static final long ITEM_NO = 12345L;
    private static final String ITEM_NAME = "테스트 상품명";
    private static final String BRAND_NAME = "테스트 브랜드";
    private static final ProductPrice PRICE = ProductPrice.of(10000, 12000, 12000, 9000, 10, 15);
    private static final ProductImages IMAGES =
            ProductImages.fromThumbnailUrls(List.of("https://img.com/thumb.jpg"));
    private static final boolean FREE_SHIPPING = true;
    private static final Instant CREATED_AT = Instant.parse("2025-01-01T00:00:00Z");

    private MiniShopCrawlData createDefault() {
        return MiniShopCrawlData.of(
                SELLER_ID,
                ITEM_NO,
                ITEM_NAME,
                BRAND_NAME,
                PRICE,
                IMAGES,
                FREE_SHIPPING,
                CREATED_AT);
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("유효한 값으로 생성한다")
        void createWithValidValues() {
            MiniShopCrawlData data = createDefault();

            assertThat(data.sellerId()).isEqualTo(SELLER_ID);
            assertThat(data.itemNo()).isEqualTo(ITEM_NO);
            assertThat(data.itemName()).isEqualTo(ITEM_NAME);
            assertThat(data.brandName()).isEqualTo(BRAND_NAME);
            assertThat(data.price()).isEqualTo(PRICE);
            assertThat(data.images()).isEqualTo(IMAGES);
            assertThat(data.freeShipping()).isTrue();
            assertThat(data.createdAt()).isEqualTo(CREATED_AT);
        }

        @Test
        @DisplayName("brandName이 null이어도 생성된다")
        void createWithNullBrandName() {
            MiniShopCrawlData data =
                    MiniShopCrawlData.of(
                            SELLER_ID, ITEM_NO, ITEM_NAME, null, PRICE, IMAGES, false, CREATED_AT);

            assertThat(data.brandName()).isNull();
        }

        @Test
        @DisplayName("freeShipping이 false이면 false로 저장된다")
        void createWithFreeShippingFalse() {
            MiniShopCrawlData data =
                    MiniShopCrawlData.of(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            PRICE,
                            IMAGES,
                            false,
                            CREATED_AT);

            assertThat(data.freeShipping()).isFalse();
        }
    }

    @Nested
    @DisplayName("생성 실패 테스트 - null 검증")
    class NullValidationTest {

        @Test
        @DisplayName("sellerId가 null이면 예외가 발생한다")
        void nullSellerIdThrowsException() {
            assertThatThrownBy(
                            () ->
                                    MiniShopCrawlData.of(
                                            null,
                                            ITEM_NO,
                                            ITEM_NAME,
                                            BRAND_NAME,
                                            PRICE,
                                            IMAGES,
                                            FREE_SHIPPING,
                                            CREATED_AT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("sellerId");
        }

        @Test
        @DisplayName("itemName이 null이면 예외가 발생한다")
        void nullItemNameThrowsException() {
            assertThatThrownBy(
                            () ->
                                    MiniShopCrawlData.of(
                                            SELLER_ID,
                                            ITEM_NO,
                                            null,
                                            BRAND_NAME,
                                            PRICE,
                                            IMAGES,
                                            FREE_SHIPPING,
                                            CREATED_AT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("itemName");
        }

        @Test
        @DisplayName("price가 null이면 예외가 발생한다")
        void nullPriceThrowsException() {
            assertThatThrownBy(
                            () ->
                                    MiniShopCrawlData.of(
                                            SELLER_ID,
                                            ITEM_NO,
                                            ITEM_NAME,
                                            BRAND_NAME,
                                            null,
                                            IMAGES,
                                            FREE_SHIPPING,
                                            CREATED_AT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("price");
        }

        @Test
        @DisplayName("images가 null이면 예외가 발생한다")
        void nullImagesThrowsException() {
            assertThatThrownBy(
                            () ->
                                    MiniShopCrawlData.of(
                                            SELLER_ID,
                                            ITEM_NO,
                                            ITEM_NAME,
                                            BRAND_NAME,
                                            PRICE,
                                            null,
                                            FREE_SHIPPING,
                                            CREATED_AT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("images");
        }

        @Test
        @DisplayName("createdAt이 null이면 예외가 발생한다")
        void nullCreatedAtThrowsException() {
            assertThatThrownBy(
                            () ->
                                    MiniShopCrawlData.of(
                                            SELLER_ID,
                                            ITEM_NO,
                                            ITEM_NAME,
                                            BRAND_NAME,
                                            PRICE,
                                            IMAGES,
                                            FREE_SHIPPING,
                                            null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("createdAt");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            MiniShopCrawlData data1 = createDefault();
            MiniShopCrawlData data2 = createDefault();

            assertThat(data1).isEqualTo(data2);
            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
        }

        @Test
        @DisplayName("다른 itemNo이면 다르다")
        void differentItemNoAreNotEqual() {
            MiniShopCrawlData data1 = createDefault();
            MiniShopCrawlData data2 =
                    MiniShopCrawlData.of(
                            SELLER_ID,
                            99999L,
                            ITEM_NAME,
                            BRAND_NAME,
                            PRICE,
                            IMAGES,
                            FREE_SHIPPING,
                            CREATED_AT);

            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("다른 sellerId이면 다르다")
        void differentSellerIdAreNotEqual() {
            MiniShopCrawlData data1 = createDefault();
            MiniShopCrawlData data2 =
                    MiniShopCrawlData.of(
                            SellerId.of(2L),
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            PRICE,
                            IMAGES,
                            FREE_SHIPPING,
                            CREATED_AT);

            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("다른 freeShipping이면 다르다")
        void differentFreeShippingAreNotEqual() {
            MiniShopCrawlData data1 = createDefault();
            MiniShopCrawlData data2 =
                    MiniShopCrawlData.of(
                            SELLER_ID,
                            ITEM_NO,
                            ITEM_NAME,
                            BRAND_NAME,
                            PRICE,
                            IMAGES,
                            false,
                            CREATED_AT);

            assertThat(data1).isNotEqualTo(data2);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("같은 파라미터로 생성하면 별개 인스턴스이지만 동등하다")
        void twoInstancesWithSameValuesAreEqualButNotSame() {
            MiniShopCrawlData data1 = createDefault();
            MiniShopCrawlData data2 = createDefault();

            assertThat(data1).isNotSameAs(data2);
            assertThat(data1).isEqualTo(data2);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString이 null을 반환하지 않는다")
        void toStringIsNotNull() {
            MiniShopCrawlData data = createDefault();
            assertThat(data.toString()).isNotNull();
            assertThat(data.toString()).contains("MiniShopCrawlData");
        }
    }
}
