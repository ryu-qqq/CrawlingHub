package com.ryuqq.crawlinghub.domain.task.input;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProductDetailTaskInputParam Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("ProductDetailTaskInputParam Value Object 단위 테스트")
class ProductDetailTaskInputParamTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("유효한 itemNo로 ProductDetailTaskInputParam 생성 성공")
        void shouldCreateWithValidItemNo() {
            // Given
            Long itemNo = 98765L;

            // When
            ProductDetailTaskInputParam param = ProductDetailTaskInputParam.of(itemNo);

            // Then
            assertThat(param).isNotNull();
            assertThat(param.getItemNo()).isEqualTo(98765L);
        }

        @Test
        @DisplayName("ProductDetailTaskInputParam은 TaskInputParam의 하위 타입이다")
        void shouldBeSubtypeOfTaskInputParam() {
            // Given
            Long itemNo = 98765L;

            // When
            ProductDetailTaskInputParam param = ProductDetailTaskInputParam.of(itemNo);

            // Then
            assertThat(param).isInstanceOf(TaskInputParam.class);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("itemNo가 null이면 예외 발생")
        void shouldThrowExceptionWhenItemNoIsNull() {
            // When & Then
            assertThatThrownBy(() -> ProductDetailTaskInputParam.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("itemNo는 필수이며 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        @DisplayName("itemNo가 0 이하면 예외 발생")
        void shouldThrowExceptionWhenItemNoIsNotPositive(Long invalidItemNo) {
            // When & Then
            assertThatThrownBy(() -> ProductDetailTaskInputParam.of(invalidItemNo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("itemNo는 필수이며 양수여야 합니다");
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 itemNo를 가진 두 ProductDetailTaskInputParam은 같다")
        void shouldBeEqualForSameItemNo() {
            // Given
            Long itemNo = 98765L;
            ProductDetailTaskInputParam param1 = ProductDetailTaskInputParam.of(itemNo);
            ProductDetailTaskInputParam param2 = ProductDetailTaskInputParam.of(itemNo);

            // When & Then
            assertThat(param1).isEqualTo(param2);
        }

        @Test
        @DisplayName("다른 itemNo를 가진 두 ProductDetailTaskInputParam은 다르다")
        void shouldNotBeEqualForDifferentItemNo() {
            // Given
            ProductDetailTaskInputParam param1 = ProductDetailTaskInputParam.of(98765L);
            ProductDetailTaskInputParam param2 = ProductDetailTaskInputParam.of(11111L);

            // When & Then
            assertThat(param1).isNotEqualTo(param2);
        }

        @Test
        @DisplayName("같은 itemNo를 가진 두 ProductDetailTaskInputParam은 같은 hashCode를 반환한다")
        void shouldReturnSameHashCodeForSameItemNo() {
            // Given
            Long itemNo = 98765L;
            ProductDetailTaskInputParam param1 = ProductDetailTaskInputParam.of(itemNo);
            ProductDetailTaskInputParam param2 = ProductDetailTaskInputParam.of(itemNo);

            // When & Then
            assertThat(param1.hashCode()).isEqualTo(param2.hashCode());
        }

        @Test
        @DisplayName("다른 itemNo를 가진 두 ProductDetailTaskInputParam은 다른 hashCode를 반환한다")
        void shouldReturnDifferentHashCodeForDifferentItemNo() {
            // Given
            ProductDetailTaskInputParam param1 = ProductDetailTaskInputParam.of(98765L);
            ProductDetailTaskInputParam param2 = ProductDetailTaskInputParam.of(11111L);

            // When & Then
            assertThat(param1.hashCode()).isNotEqualTo(param2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 itemNo를 포함한다")
        void shouldIncludeItemNoInToString() {
            // Given
            Long itemNo = 98765L;
            ProductDetailTaskInputParam param = ProductDetailTaskInputParam.of(itemNo);

            // When
            String result = param.toString();

            // Then
            assertThat(result).contains("itemNo=98765");
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("ProductDetailTaskInputParam은 생성 후 상태를 변경할 수 없다")
        void shouldBeImmutable() {
            // Given
            Long itemNo = 98765L;
            ProductDetailTaskInputParam param = ProductDetailTaskInputParam.of(itemNo);

            // When
            Long retrievedItemNo = param.getItemNo();

            // Then: 여러 번 호출해도 같은 값
            assertThat(param.getItemNo()).isEqualTo(retrievedItemNo);
        }
    }
}
