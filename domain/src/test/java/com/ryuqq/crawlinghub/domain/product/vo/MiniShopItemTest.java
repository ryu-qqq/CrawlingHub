package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * MiniShopItem VO 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("MiniShopItem 테스트")
class MiniShopItemTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 값으로 MiniShopItem 생성")
        void shouldCreateWithValidValues() {
            // given
            List<String> images = List.of("https://example.com/img1.jpg");
            List<ItemTag> tags = List.of(ItemTag.ofTitle("무료배송"));

            // when
            MiniShopItem item =
                    new MiniShopItem(
                            1L,
                            images,
                            "Brand",
                            "Product Name",
                            10000,
                            12000,
                            12000,
                            20,
                            25,
                            9500,
                            tags);

            // then
            assertThat(item.itemNo()).isEqualTo(1L);
            assertThat(item.brandName()).isEqualTo("Brand");
            assertThat(item.name()).isEqualTo("Product Name");
            assertThat(item.price()).isEqualTo(10000);
            assertThat(item.discountRate()).isEqualTo(20);
        }

        @Test
        @DisplayName("null 리스트들은 빈 리스트로 변환")
        void shouldConvertNullListsToEmpty() {
            // given & when
            MiniShopItem item =
                    new MiniShopItem(1L, null, "Brand", "Product", 10000, 0, 0, 0, 0, 0, null);

            // then
            assertThat(item.imageUrlList()).isEmpty();
            assertThat(item.tagList()).isEmpty();
        }

        @Test
        @DisplayName("리스트는 방어적 복사됨")
        void shouldDefensivelyCopyLists() {
            // given
            List<String> originalImages = new java.util.ArrayList<>();
            originalImages.add("https://example.com/img.jpg");

            // when
            MiniShopItem item =
                    new MiniShopItem(
                            1L, originalImages, "Brand", "Product", 10000, 0, 0, 0, 0, 0, null);

            // then
            assertThatThrownBy(() -> item.imageUrlList().add("new"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("itemNo가 null이면 예외 발생")
        void shouldThrowWhenItemNoIsNull() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    new MiniShopItem(
                                            null, null, "Brand", "Product", 0, 0, 0, 0, 0, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemNo는 필수");
        }

        @Test
        @DisplayName("name이 null이면 예외 발생")
        void shouldThrowWhenNameIsNull() {
            // given & when & then
            assertThatThrownBy(
                            () -> new MiniShopItem(1L, null, "Brand", null, 0, 0, 0, 0, 0, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name은 필수");
        }

        @Test
        @DisplayName("name이 빈 문자열이면 예외 발생")
        void shouldThrowWhenNameIsBlank() {
            // given & when & then
            assertThatThrownBy(
                            () -> new MiniShopItem(1L, null, "Brand", "  ", 0, 0, 0, 0, 0, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name은 필수");
        }

        @Test
        @DisplayName("price가 음수면 예외 발생")
        void shouldThrowWhenPriceIsNegative() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    new MiniShopItem(
                                            1L, null, "Brand", "Product", -1, 0, 0, 0, 0, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("price는 0 이상");
        }

        @Test
        @DisplayName("discountRate가 범위 밖이면 예외 발생")
        void shouldThrowWhenDiscountRateOutOfRange() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    new MiniShopItem(
                                            1L, null, "Brand", "Product", 0, 0, 0, -1, 0, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("discountRate는 0~100");

            assertThatThrownBy(
                            () ->
                                    new MiniShopItem(
                                            1L, null, "Brand", "Product", 0, 0, 0, 101, 0, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("discountRate는 0~100");
        }
    }

    @Nested
    @DisplayName("정적 파싱 메서드 테스트")
    class ParsingMethodTest {

        @Test
        @DisplayName("가격 문자열 파싱")
        void shouldParsePriceString() {
            // given & when & then
            assertThat(MiniShopItem.parsePrice("1,075,000")).isEqualTo(1075000);
            assertThat(MiniShopItem.parsePrice("545,600")).isEqualTo(545600);
            assertThat(MiniShopItem.parsePrice("1000")).isEqualTo(1000);
            assertThat(MiniShopItem.parsePrice(null)).isZero();
            assertThat(MiniShopItem.parsePrice("")).isZero();
            assertThat(MiniShopItem.parsePrice("invalid")).isZero();
        }

        @Test
        @DisplayName("할인율 문자열 파싱")
        void shouldParseDiscountRateString() {
            // given & when & then
            assertThat(MiniShopItem.parseDiscountRate("49")).isEqualTo(49);
            assertThat(MiniShopItem.parseDiscountRate("0")).isZero();
            assertThat(MiniShopItem.parseDiscountRate(null)).isZero();
            assertThat(MiniShopItem.parseDiscountRate("")).isZero();
            assertThat(MiniShopItem.parseDiscountRate("invalid")).isZero();
        }

        @Test
        @DisplayName("문자열 값들로 MiniShopItem 생성")
        void shouldCreateFromStrings() {
            // given & when
            MiniShopItem item =
                    MiniShopItem.fromStrings(
                            1L,
                            null,
                            "Brand",
                            "Product",
                            "545,600",
                            "1,075,000",
                            "1,075,000",
                            "49",
                            "49",
                            "545,600",
                            null);

            // then
            assertThat(item.price()).isEqualTo(545600);
            assertThat(item.originalPrice()).isEqualTo(1075000);
            assertThat(item.discountRate()).isEqualTo(49);
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    class BusinessMethodTest {

        @Test
        @DisplayName("대표 이미지 URL 반환")
        void shouldReturnMainImageUrl() {
            // given
            List<String> images =
                    List.of("https://example.com/img1.jpg", "https://example.com/img2.jpg");
            MiniShopItem item =
                    new MiniShopItem(1L, images, "Brand", "Product", 0, 0, 0, 0, 0, 0, null);
            MiniShopItem noImage =
                    new MiniShopItem(2L, null, "Brand", "Product", 0, 0, 0, 0, 0, 0, null);

            // when & then
            assertThat(item.mainImageUrl()).isEqualTo("https://example.com/img1.jpg");
            assertThat(noImage.mainImageUrl()).isNull();
        }

        @Test
        @DisplayName("할인 여부 확인")
        void shouldCheckHasDiscount() {
            // given
            MiniShopItem discounted =
                    new MiniShopItem(
                            1L, null, "Brand", "Product", 10000, 12000, 12000, 20, 0, 0, null);
            MiniShopItem notDiscounted =
                    new MiniShopItem(
                            2L, null, "Brand", "Product", 10000, 10000, 10000, 0, 0, 0, null);

            // when & then
            assertThat(discounted.hasDiscount()).isTrue();
            assertThat(notDiscounted.hasDiscount()).isFalse();
        }

        @Test
        @DisplayName("앱 전용 할인 확인")
        void shouldCheckHasAppExclusiveDiscount() {
            // given
            MiniShopItem appExclusive =
                    new MiniShopItem(
                            1L, null, "Brand", "Product", 10000, 12000, 12000, 20, 25, 9500, null);
            MiniShopItem noAppExclusive =
                    new MiniShopItem(
                            2L, null, "Brand", "Product", 10000, 12000, 12000, 20, 20, 10000, null);

            // when & then
            assertThat(appExclusive.hasAppExclusiveDiscount()).isTrue();
            assertThat(noAppExclusive.hasAppExclusiveDiscount()).isFalse();
        }

        @Test
        @DisplayName("ProductPrice로 변환")
        void shouldConvertToProductPrice() {
            // given
            MiniShopItem item =
                    new MiniShopItem(
                            1L, null, "Brand", "Product", 10000, 12000, 12000, 20, 25, 9500, null);

            // when
            ProductPrice price = item.toProductPrice();

            // then
            assertThat(price.price()).isEqualTo(10000);
            assertThat(price.originalPrice()).isEqualTo(12000);
            assertThat(price.discountRate()).isEqualTo(20);
            assertThat(price.appDiscountRate()).isEqualTo(25);
        }

        @Test
        @DisplayName("무료배송 태그 확인")
        void shouldCheckHasFreeShippingTag() {
            // given
            List<ItemTag> freeShippingTags = List.of(ItemTag.ofTitle("무료배송"));
            List<ItemTag> freeEnglishTags = List.of(ItemTag.ofTitle("FREE SHIPPING"));
            List<ItemTag> noFreeShipping = List.of(ItemTag.ofTitle("할인"));

            MiniShopItem withFreeShipping =
                    new MiniShopItem(
                            1L, null, "Brand", "Product", 0, 0, 0, 0, 0, 0, freeShippingTags);
            MiniShopItem withFreeEnglish =
                    new MiniShopItem(
                            2L, null, "Brand", "Product", 0, 0, 0, 0, 0, 0, freeEnglishTags);
            MiniShopItem withoutFreeShipping =
                    new MiniShopItem(
                            3L, null, "Brand", "Product", 0, 0, 0, 0, 0, 0, noFreeShipping);
            MiniShopItem noTags =
                    new MiniShopItem(4L, null, "Brand", "Product", 0, 0, 0, 0, 0, 0, null);

            // when & then
            assertThat(withFreeShipping.hasFreeShippingTag()).isTrue();
            assertThat(withFreeEnglish.hasFreeShippingTag()).isTrue();
            assertThat(withoutFreeShipping.hasFreeShippingTag()).isFalse();
            assertThat(noTags.hasFreeShippingTag()).isFalse();
        }

        @Test
        @DisplayName("이미지 URL 목록 반환")
        void shouldReturnImageUrls() {
            // given
            List<String> images = List.of("https://example.com/img1.jpg");
            MiniShopItem item =
                    new MiniShopItem(1L, images, "Brand", "Product", 0, 0, 0, 0, 0, 0, null);

            // when & then
            assertThat(item.imageUrls()).isEqualTo(images);
        }
    }
}
