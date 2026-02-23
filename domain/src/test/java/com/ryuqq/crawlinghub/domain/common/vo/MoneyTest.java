package com.ryuqq.crawlinghub.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("Money Value Object 단위 테스트")
class MoneyTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("양수 금액으로 생성한다")
        void createWithPositiveValue() {
            // when
            Money money = Money.of(10000);

            // then
            assertThat(money.value()).isEqualTo(10000);
        }

        @Test
        @DisplayName("0으로 생성한다")
        void createWithZero() {
            // when
            Money money = Money.of(0);

            // then
            assertThat(money.value()).isEqualTo(0);
        }

        @Test
        @DisplayName("zero() 팩토리 메서드로 0을 생성한다")
        void createWithZeroFactory() {
            // when
            Money money = Money.zero();

            // then
            assertThat(money.value()).isEqualTo(0);
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("음수 금액으로 생성하면 예외가 발생한다")
        void throwWhenNegative() {
            assertThatThrownBy(() -> Money.of(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }
    }

    @Nested
    @DisplayName("add() 덧셈 테스트")
    class AddTest {

        @Test
        @DisplayName("두 금액을 더한다")
        void addTwoMoneys() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(5000);

            // when
            Money result = money1.add(money2);

            // then
            assertThat(result.value()).isEqualTo(15000);
        }

        @Test
        @DisplayName("0을 더해도 같은 금액이다")
        void addZero() {
            // given
            Money money = Money.of(10000);

            // when
            Money result = money.add(Money.zero());

            // then
            assertThat(result.value()).isEqualTo(10000);
        }
    }

    @Nested
    @DisplayName("subtract() 뺄셈 테스트")
    class SubtractTest {

        @Test
        @DisplayName("두 금액을 뺀다")
        void subtractTwoMoneys() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(3000);

            // when
            Money result = money1.subtract(money2);

            // then
            assertThat(result.value()).isEqualTo(7000);
        }

        @Test
        @DisplayName("같은 금액을 빼면 0이 된다")
        void subtractSameAmountResultsInZero() {
            // given
            Money money = Money.of(5000);

            // when
            Money result = money.subtract(money);

            // then
            assertThat(result.isZero()).isTrue();
        }

        @Test
        @DisplayName("더 큰 금액을 빼면 예외가 발생한다")
        void throwWhenResultIsNegative() {
            // given
            Money money1 = Money.of(3000);
            Money money2 = Money.of(10000);

            // when & then
            assertThatThrownBy(() -> money1.subtract(money2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("음수");
        }
    }

    @Nested
    @DisplayName("multiply() 곱셈 테스트")
    class MultiplyTest {

        @Test
        @DisplayName("금액에 배수를 곱한다")
        void multiplyByPositive() {
            // given
            Money money = Money.of(1000);

            // when
            Money result = money.multiply(3);

            // then
            assertThat(result.value()).isEqualTo(3000);
        }

        @Test
        @DisplayName("0을 곱하면 0이 된다")
        void multiplyByZero() {
            // given
            Money money = Money.of(10000);

            // when
            Money result = money.multiply(0);

            // then
            assertThat(result.isZero()).isTrue();
        }

        @Test
        @DisplayName("음수를 곱하면 예외가 발생한다")
        void throwWhenMultiplierIsNegative() {
            // given
            Money money = Money.of(1000);

            // when & then
            assertThatThrownBy(() -> money.multiply(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }
    }

    @Nested
    @DisplayName("비교 메서드 테스트")
    class ComparisonTest {

        @Test
        @DisplayName("isGreaterThan - 더 큰 금액이면 true")
        void isGreaterThanReturnsTrue() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(5000);

            // then
            assertThat(money1.isGreaterThan(money2)).isTrue();
        }

        @Test
        @DisplayName("isGreaterThan - 작거나 같으면 false")
        void isGreaterThanReturnsFalse() {
            // given
            Money money1 = Money.of(5000);
            Money money2 = Money.of(10000);

            // then
            assertThat(money1.isGreaterThan(money2)).isFalse();
        }

        @Test
        @DisplayName("isGreaterThanOrEqual - 같으면 true")
        void isGreaterThanOrEqualWhenSame() {
            // given
            Money money = Money.of(5000);

            // then
            assertThat(money.isGreaterThanOrEqual(money)).isTrue();
        }

        @Test
        @DisplayName("isLessThan - 더 작은 금액이면 true")
        void isLessThanReturnsTrue() {
            // given
            Money money1 = Money.of(3000);
            Money money2 = Money.of(5000);

            // then
            assertThat(money1.isLessThan(money2)).isTrue();
        }

        @Test
        @DisplayName("isLessThanOrEqual - 같으면 true")
        void isLessThanOrEqualWhenSame() {
            // given
            Money money = Money.of(5000);

            // then
            assertThat(money.isLessThanOrEqual(money)).isTrue();
        }
    }

    @Nested
    @DisplayName("discountRate() 할인율 계산 테스트")
    class DiscountRateTest {

        @Test
        @DisplayName("50% 할인율을 계산한다")
        void calculate50PercentDiscount() {
            // given
            Money regular = Money.of(10000);
            Money current = Money.of(5000);

            // when
            int rate = Money.discountRate(regular, current);

            // then
            assertThat(rate).isEqualTo(50);
        }

        @Test
        @DisplayName("정가가 0이면 0%를 반환한다")
        void returnZeroWhenRegularIsZero() {
            // given
            Money regular = Money.zero();
            Money current = Money.of(5000);

            // when
            int rate = Money.discountRate(regular, current);

            // then
            assertThat(rate).isEqualTo(0);
        }

        @Test
        @DisplayName("현재가가 정가 이상이면 0%를 반환한다")
        void returnZeroWhenCurrentIsGreaterThanOrEqualToRegular() {
            // given
            Money regular = Money.of(5000);
            Money current = Money.of(5000);

            // when
            int rate = Money.discountRate(regular, current);

            // then
            assertThat(rate).isEqualTo(0);
        }

        @Test
        @DisplayName("현재가가 정가보다 크면 0%를 반환한다")
        void returnZeroWhenCurrentExceedsRegular() {
            // given
            Money regular = Money.of(5000);
            Money current = Money.of(8000);

            // when
            int rate = Money.discountRate(regular, current);

            // then
            assertThat(rate).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 금액이면 동일하다")
        void sameValueAreEqual() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(10000);

            // then
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("다른 금액이면 다르다")
        void differentValueAreNotEqual() {
            // given
            Money money1 = Money.of(10000);
            Money money2 = Money.of(5000);

            // then
            assertThat(money1).isNotEqualTo(money2);
        }
    }
}
