package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ProductOption VO 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProductOption 테스트")
class ProductOptionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 값으로 ProductOption 생성")
        void shouldCreateWithValidValues() {
            // given & when
            ProductOption option = ProductOption.of(1L, 100L, "Red", "M", 10, "Size Guide");

            // then
            assertThat(option.optionNo()).isEqualTo(1L);
            assertThat(option.itemNo()).isEqualTo(100L);
            assertThat(option.color()).isEqualTo("Red");
            assertThat(option.size()).isEqualTo("M");
            assertThat(option.stock()).isEqualTo(10);
            assertThat(option.sizeGuide()).isEqualTo("Size Guide");
        }

        @Test
        @DisplayName("null 값들은 빈 문자열로 변환")
        void shouldConvertNullToEmptyString() {
            // given & when
            ProductOption option = ProductOption.of(1L, 100L, null, null, 10, null);

            // then
            assertThat(option.color()).isEmpty();
            assertThat(option.size()).isEmpty();
            assertThat(option.sizeGuide()).isEmpty();
        }

        @Test
        @DisplayName("optionNo가 0 이하면 예외 발생")
        void shouldThrowWhenOptionNoIsNotPositive() {
            // given & when & then
            assertThatThrownBy(() -> ProductOption.of(0L, 100L, "Red", "M", 10, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("optionNo는 양수");

            assertThatThrownBy(() -> ProductOption.of(-1L, 100L, "Red", "M", 10, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("optionNo는 양수");
        }

        @Test
        @DisplayName("itemNo가 0 이하면 예외 발생")
        void shouldThrowWhenItemNoIsNotPositive() {
            // given & when & then
            assertThatThrownBy(() -> ProductOption.of(1L, 0L, "Red", "M", 10, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemNo는 양수");
        }

        @Test
        @DisplayName("stock이 음수면 예외 발생")
        void shouldThrowWhenStockIsNegative() {
            // given & when & then
            assertThatThrownBy(() -> ProductOption.of(1L, 100L, "Red", "M", -1, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("stock은 0 이상");
        }
    }

    @Nested
    @DisplayName("재고 관련 메서드 테스트")
    class StockMethodTest {

        @Test
        @DisplayName("재고 있음 확인")
        void shouldCheckIsInStock() {
            // given
            ProductOption inStock = ProductOption.of(1L, 100L, "Red", "M", 10, null);
            ProductOption outOfStock = ProductOption.of(2L, 100L, "Red", "M", 0, null);

            // when & then
            assertThat(inStock.isInStock()).isTrue();
            assertThat(outOfStock.isInStock()).isFalse();
        }

        @Test
        @DisplayName("품절 확인")
        void shouldCheckIsSoldOut() {
            // given
            ProductOption inStock = ProductOption.of(1L, 100L, "Red", "M", 10, null);
            ProductOption outOfStock = ProductOption.of(2L, 100L, "Red", "M", 0, null);

            // when & then
            assertThat(inStock.isSoldOut()).isFalse();
            assertThat(outOfStock.isSoldOut()).isTrue();
        }

        @Test
        @DisplayName("재고 변경 여부 확인")
        void shouldCheckStockChange() {
            // given
            ProductOption original = ProductOption.of(1L, 100L, "Red", "M", 10, null);
            ProductOption sameStock = ProductOption.of(1L, 100L, "Red", "M", 10, null);
            ProductOption differentStock = ProductOption.of(1L, 100L, "Red", "M", 5, null);

            // when & then
            assertThat(original.hasStockChange(sameStock)).isFalse();
            assertThat(original.hasStockChange(differentStock)).isTrue();
            assertThat(original.hasStockChange(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("속성 확인 메서드 테스트")
    class AttributeMethodTest {

        @Test
        @DisplayName("색상 있음 확인")
        void shouldCheckHasColor() {
            // given
            ProductOption withColor = ProductOption.of(1L, 100L, "Red", "M", 10, null);
            ProductOption withoutColor = ProductOption.of(2L, 100L, "", "M", 10, null);
            ProductOption blankColor = ProductOption.of(3L, 100L, "  ", "M", 10, null);

            // when & then
            assertThat(withColor.hasColor()).isTrue();
            assertThat(withoutColor.hasColor()).isFalse();
            assertThat(blankColor.hasColor()).isFalse();
        }

        @Test
        @DisplayName("사이즈 있음 확인")
        void shouldCheckHasSize() {
            // given
            ProductOption withSize = ProductOption.of(1L, 100L, "Red", "M", 10, null);
            ProductOption withoutSize = ProductOption.of(2L, 100L, "Red", "", 10, null);
            ProductOption blankSize = ProductOption.of(3L, 100L, "Red", "  ", 10, null);

            // when & then
            assertThat(withSize.hasSize()).isTrue();
            assertThat(withoutSize.hasSize()).isFalse();
            assertThat(blankSize.hasSize()).isFalse();
        }
    }
}
