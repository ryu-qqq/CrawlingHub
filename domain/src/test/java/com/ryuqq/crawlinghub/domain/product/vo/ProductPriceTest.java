package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ProductPrice VO 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProductPrice 테스트")
class ProductPriceTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 값으로 ProductPrice 생성")
        void shouldCreateWithValidValues() {
            // given & when
            ProductPrice price = ProductPrice.of(10000, 12000, 12000, 9500, 20, 25);

            // then
            assertThat(price.price()).isEqualTo(10000);
            assertThat(price.originalPrice()).isEqualTo(12000);
            assertThat(price.normalPrice()).isEqualTo(12000);
            assertThat(price.appPrice()).isEqualTo(9500);
            assertThat(price.discountRate()).isEqualTo(20);
            assertThat(price.appDiscountRate()).isEqualTo(25);
        }

        @Test
        @DisplayName("price가 음수면 예외 발생")
        void shouldThrowWhenPriceIsNegative() {
            // given & when & then
            assertThatThrownBy(() -> ProductPrice.of(-1, 0, 0, 0, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("price는 0 이상");
        }

        @Test
        @DisplayName("originalPrice가 음수면 예외 발생")
        void shouldThrowWhenOriginalPriceIsNegative() {
            // given & when & then
            assertThatThrownBy(() -> ProductPrice.of(0, -1, 0, 0, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("originalPrice는 0 이상");
        }

        @Test
        @DisplayName("normalPrice가 음수면 예외 발생")
        void shouldThrowWhenNormalPriceIsNegative() {
            // given & when & then
            assertThatThrownBy(() -> ProductPrice.of(0, 0, -1, 0, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("normalPrice는 0 이상");
        }

        @Test
        @DisplayName("appPrice가 음수면 예외 발생")
        void shouldThrowWhenAppPriceIsNegative() {
            // given & when & then
            assertThatThrownBy(() -> ProductPrice.of(0, 0, 0, -1, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("appPrice는 0 이상");
        }

        @Test
        @DisplayName("discountRate가 범위 밖이면 예외 발생")
        void shouldThrowWhenDiscountRateOutOfRange() {
            // given & when & then
            assertThatThrownBy(() -> ProductPrice.of(0, 0, 0, 0, -1, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("discountRate는 0~100");

            assertThatThrownBy(() -> ProductPrice.of(0, 0, 0, 0, 101, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("discountRate는 0~100");
        }

        @Test
        @DisplayName("appDiscountRate가 범위 밖이면 예외 발생")
        void shouldThrowWhenAppDiscountRateOutOfRange() {
            // given & when & then
            assertThatThrownBy(() -> ProductPrice.of(0, 0, 0, 0, 0, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("appDiscountRate는 0~100");

            assertThatThrownBy(() -> ProductPrice.of(0, 0, 0, 0, 0, 101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("appDiscountRate는 0~100");
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    class BusinessMethodTest {

        @Test
        @DisplayName("할인 중인지 확인")
        void shouldCheckHasDiscount() {
            // given
            ProductPrice discounted = ProductPrice.of(10000, 12000, 12000, 9500, 20, 25);
            ProductPrice notDiscounted = ProductPrice.of(10000, 10000, 10000, 10000, 0, 0);

            // when & then
            assertThat(discounted.hasDiscount()).isTrue();
            assertThat(notDiscounted.hasDiscount()).isFalse();
        }

        @Test
        @DisplayName("앱 전용 추가 할인 확인")
        void shouldCheckHasAppExclusiveDiscount() {
            // given
            ProductPrice appExclusive = ProductPrice.of(10000, 12000, 12000, 9500, 20, 25);
            ProductPrice noAppExclusive = ProductPrice.of(10000, 12000, 12000, 9500, 25, 25);

            // when & then
            assertThat(appExclusive.hasAppExclusiveDiscount()).isTrue();
            assertThat(noAppExclusive.hasAppExclusiveDiscount()).isFalse();
        }

        @Test
        @DisplayName("판매가 반환")
        void shouldReturnSellingPrice() {
            // given
            ProductPrice price = ProductPrice.of(10000, 12000, 12000, 9500, 20, 25);

            // when & then
            assertThat(price.sellingPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("할인가 반환 - 앱 가격 우선")
        void shouldReturnDiscountPriceWithAppPricePriority() {
            // given
            ProductPrice withAppPrice = ProductPrice.of(10000, 12000, 12000, 9500, 20, 25);
            ProductPrice withoutAppPrice = ProductPrice.of(10000, 12000, 12000, 0, 20, 0);

            // when & then
            assertThat(withAppPrice.discountPrice()).isEqualTo(9500);
            assertThat(withoutAppPrice.discountPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("가격 변경 여부 확인")
        void shouldCheckPriceChange() {
            // given
            ProductPrice original = ProductPrice.of(10000, 12000, 12000, 9500, 20, 25);
            ProductPrice same = ProductPrice.of(10000, 12000, 12000, 9500, 20, 25);
            ProductPrice different = ProductPrice.of(11000, 12000, 12000, 9500, 20, 25);

            // when & then
            assertThat(original.hasPriceChange(same)).isFalse();
            assertThat(original.hasPriceChange(different)).isTrue();
            assertThat(original.hasPriceChange(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("MiniShopItem에서 ProductPrice 생성")
        void shouldCreateFromMiniShopItem() {
            // given
            MiniShopItem item =
                    new MiniShopItem(
                            1L, null, "Brand", "Product", 10000, 12000, 12000, 20, 25, 9500, null);

            // when
            ProductPrice price = ProductPrice.fromMiniShopItem(item);

            // then
            assertThat(price.price()).isEqualTo(10000);
            assertThat(price.originalPrice()).isEqualTo(12000);
            assertThat(price.discountRate()).isEqualTo(20);
            assertThat(price.appDiscountRate()).isEqualTo(25);
        }
    }
}
