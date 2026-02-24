package com.ryuqq.crawlinghub.domain.task.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("RetryCount Value Object 단위 테스트")
class RetryCountTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("0으로 생성한다")
        void createWithZero() {
            RetryCount count = new RetryCount(0);
            assertThat(count.value()).isEqualTo(0);
        }

        @Test
        @DisplayName("최대값(2)으로 생성한다")
        void createWithMaxValue() {
            RetryCount count = new RetryCount(RetryCount.MAX_RETRY_COUNT);
            assertThat(count.value()).isEqualTo(2);
        }

        @Test
        @DisplayName("음수이면 예외가 발생한다")
        void negativeValueThrowsException() {
            assertThatThrownBy(() -> new RetryCount(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }

        @Test
        @DisplayName("최대값을 초과하면 예외가 발생한다")
        void exceedMaxValueThrowsException() {
            assertThatThrownBy(() -> new RetryCount(3))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최대");
        }
    }

    @Nested
    @DisplayName("zero() 팩토리 메서드 테스트")
    class ZeroFactoryTest {

        @Test
        @DisplayName("0을 반환한다")
        void returnsZero() {
            RetryCount count = RetryCount.zero();
            assertThat(count.value()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("canRetry() 테스트")
    class CanRetryTest {

        @Test
        @DisplayName("0이면 재시도 가능하다")
        void zeroCanRetry() {
            RetryCount count = RetryCount.zero();
            assertThat(count.canRetry()).isTrue();
        }

        @Test
        @DisplayName("1이면 재시도 가능하다")
        void oneCanRetry() {
            RetryCount count = new RetryCount(1);
            assertThat(count.canRetry()).isTrue();
        }

        @Test
        @DisplayName("최대값(2)이면 재시도 불가능하다")
        void maxValueCannotRetry() {
            RetryCount count = new RetryCount(RetryCount.MAX_RETRY_COUNT);
            assertThat(count.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("increment() 테스트")
    class IncrementTest {

        @Test
        @DisplayName("0에서 1로 증가한다")
        void incrementFromZeroToOne() {
            RetryCount count = RetryCount.zero();
            RetryCount incremented = count.increment();
            assertThat(incremented.value()).isEqualTo(1);
        }

        @Test
        @DisplayName("1에서 2로 증가한다")
        void incrementFromOneToTwo() {
            RetryCount count = new RetryCount(1);
            RetryCount incremented = count.increment();
            assertThat(incremented.value()).isEqualTo(2);
        }

        @Test
        @DisplayName("최대값에서 증가하면 예외가 발생한다")
        void incrementFromMaxThrowsException() {
            RetryCount count = new RetryCount(RetryCount.MAX_RETRY_COUNT);
            assertThatThrownBy(count::increment).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("원본 불변성 - increment는 새 인스턴스를 반환한다")
        void incrementReturnNewInstance() {
            RetryCount original = RetryCount.zero();
            RetryCount incremented = original.increment();
            assertThat(original.value()).isEqualTo(0);
            assertThat(incremented.value()).isEqualTo(1);
            assertThat(original).isNotSameAs(incremented);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValueAreEqual() {
            RetryCount count1 = new RetryCount(1);
            RetryCount count2 = new RetryCount(1);
            assertThat(count1).isEqualTo(count2);
            assertThat(count1.hashCode()).isEqualTo(count2.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 다르다")
        void differentValuesAreNotEqual() {
            RetryCount count1 = new RetryCount(0);
            RetryCount count2 = new RetryCount(1);
            assertThat(count1).isNotEqualTo(count2);
        }
    }
}
