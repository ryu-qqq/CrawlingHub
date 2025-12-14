package com.ryuqq.crawlinghub.domain.product.identifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
@Tag("domain")
@Tag("identifier")
@DisplayName("CrawledProductId 단위 테스트")
class CrawledProductIdTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("양수 값으로 생성할 수 있다")
        void shouldCreateWithPositiveValue() {
            // When
            CrawledProductId id = new CrawledProductId(1L);

            // Then
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null 값으로 생성할 수 있다 (미할당 상태)")
        void shouldCreateWithNullValue() {
            // When
            CrawledProductId id = new CrawledProductId(null);

            // Then
            assertThat(id.value()).isNull();
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L, Long.MIN_VALUE})
        @DisplayName("0 이하 값이면 예외를 던진다")
        void shouldThrowWhenValueIsZeroOrNegative(Long value) {
            // When & Then
            assertThatThrownBy(() -> new CrawledProductId(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }
    }

    @Nested
    @DisplayName("unassigned 팩토리 메서드")
    class UnassignedMethod {

        @Test
        @DisplayName("미할당 ID를 생성한다")
        void shouldCreateUnassignedId() {
            // When
            CrawledProductId id = CrawledProductId.unassigned();

            // Then
            assertThat(id.value()).isNull();
            assertThat(id.isAssigned()).isFalse();
        }
    }

    @Nested
    @DisplayName("of 팩토리 메서드")
    class OfMethod {

        @Test
        @DisplayName("유효한 값으로 ID를 생성한다")
        void shouldCreateIdWithValidValue() {
            // When
            CrawledProductId id = CrawledProductId.of(100L);

            // Then
            assertThat(id.value()).isEqualTo(100L);
            assertThat(id.isAssigned()).isTrue();
        }

        @Test
        @DisplayName("null 값이면 예외를 던진다")
        void shouldThrowWhenValueIsNull() {
            // When & Then
            assertThatThrownBy(() -> CrawledProductId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("0 이하 값이면 예외를 던진다")
        void shouldThrowWhenValueIsZeroOrNegative(Long value) {
            // When & Then
            assertThatThrownBy(() -> CrawledProductId.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }
    }

    @Nested
    @DisplayName("isAssigned 메서드")
    class IsAssignedMethod {

        @Test
        @DisplayName("값이 있으면 true를 반환한다")
        void shouldReturnTrueWhenValueExists() {
            // Given
            CrawledProductId id = CrawledProductId.of(1L);

            // When & Then
            assertThat(id.isAssigned()).isTrue();
        }

        @Test
        @DisplayName("값이 null이면 false를 반환한다")
        void shouldReturnFalseWhenValueIsNull() {
            // Given
            CrawledProductId id = CrawledProductId.unassigned();

            // When & Then
            assertThat(id.isAssigned()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 값을 가진 ID는 동등하다")
        void shouldBeEqualForSameValue() {
            // Given
            CrawledProductId id1 = CrawledProductId.of(1L);
            CrawledProductId id2 = CrawledProductId.of(1L);

            // When & Then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ID는 동등하지 않다")
        void shouldNotBeEqualForDifferentValue() {
            // Given
            CrawledProductId id1 = CrawledProductId.of(1L);
            CrawledProductId id2 = CrawledProductId.of(2L);

            // When & Then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("미할당 ID끼리는 동등하다")
        void shouldBeEqualForUnassignedIds() {
            // Given
            CrawledProductId id1 = CrawledProductId.unassigned();
            CrawledProductId id2 = CrawledProductId.unassigned();

            // When & Then
            assertThat(id1).isEqualTo(id2);
        }
    }
}
