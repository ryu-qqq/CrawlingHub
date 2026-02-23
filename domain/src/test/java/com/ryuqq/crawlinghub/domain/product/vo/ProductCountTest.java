package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("ProductCount 단위 테스트")
class ProductCountTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("0으로 생성한다")
        void createWithZero() {
            // when
            ProductCount count = ProductCount.of(0);

            // then
            assertThat(count.totalCount()).isEqualTo(0);
            assertThat(count.hasProducts()).isFalse();
        }

        @Test
        @DisplayName("양수로 생성한다")
        void createWithPositive() {
            // when
            ProductCount count = ProductCount.of(1000);

            // then
            assertThat(count.totalCount()).isEqualTo(1000);
            assertThat(count.hasProducts()).isTrue();
        }

        @Test
        @DisplayName("음수로 생성하면 예외가 발생한다")
        void throwWhenNegative() {
            assertThatThrownBy(() -> ProductCount.of(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }
    }

    @Nested
    @DisplayName("calculateTotalPages() 메서드 테스트")
    class CalculateTotalPagesTest {

        @Test
        @DisplayName("상품이 0개이면 페이지는 0이다")
        void zeroProductsHasZeroPages() {
            assertThat(ProductCount.of(0).calculateTotalPages()).isEqualTo(0);
        }

        @Test
        @DisplayName("500개이면 1페이지이다")
        void fiveHundredProductsHasOnePage() {
            assertThat(ProductCount.of(500).calculateTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("501개이면 2페이지이다 (올림)")
        void fiveHundredOneProductsHasTwoPages() {
            assertThat(ProductCount.of(501).calculateTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("1000개이면 2페이지이다")
        void oneThousandProductsHasTwoPages() {
            assertThat(ProductCount.of(1000).calculateTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("1001개이면 3페이지이다")
        void oneThousandOneProductsHasThreePages() {
            assertThat(ProductCount.of(1001).calculateTotalPages()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("hasProducts() 메서드 테스트")
    class HasProductsTest {

        @Test
        @DisplayName("0개이면 false")
        void noProductsReturnsFalse() {
            assertThat(ProductCount.of(0).hasProducts()).isFalse();
        }

        @Test
        @DisplayName("1개 이상이면 true")
        void someProductsReturnsTrue() {
            assertThat(ProductCount.of(1).hasProducts()).isTrue();
            assertThat(ProductCount.of(100).hasProducts()).isTrue();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValueAreEqual() {
            // given
            ProductCount count1 = ProductCount.of(100);
            ProductCount count2 = ProductCount.of(100);

            // then
            assertThat(count1).isEqualTo(count2);
            assertThat(count1.hashCode()).isEqualTo(count2.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 다르다")
        void differentValueAreNotEqual() {
            // given
            ProductCount count1 = ProductCount.of(100);
            ProductCount count2 = ProductCount.of(200);

            // then
            assertThat(count1).isNotEqualTo(count2);
        }
    }
}
