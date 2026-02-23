package com.ryuqq.crawlinghub.domain.product.id;

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
@DisplayName("CrawledRawId 단위 테스트")
class CrawledRawIdTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("양수 값으로 생성할 수 있다")
        void shouldCreateWithPositiveValue() {
            // When
            CrawledRawId id = new CrawledRawId(1L);

            // Then
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null 값으로 생성할 수 있다 (미할당 상태)")
        void shouldCreateWithNullValue() {
            // When
            CrawledRawId id = new CrawledRawId(null);

            // Then
            assertThat(id.value()).isNull();
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L, Long.MIN_VALUE})
        @DisplayName("0 이하 값이면 예외를 던진다")
        void shouldThrowWhenValueIsZeroOrNegative(Long value) {
            // When & Then
            assertThatThrownBy(() -> new CrawledRawId(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }
    }

    @Nested
    @DisplayName("forNew 팩토리 메서드")
    class ForNewMethod {

        @Test
        @DisplayName("신규 ID를 생성한다")
        void shouldCreateForNewId() {
            // When
            CrawledRawId id = CrawledRawId.forNew();

            // Then
            assertThat(id.value()).isNull();
            assertThat(id.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("of 팩토리 메서드")
    class OfMethod {

        @Test
        @DisplayName("유효한 값으로 ID를 생성한다")
        void shouldCreateIdWithValidValue() {
            // When
            CrawledRawId id = CrawledRawId.of(100L);

            // Then
            assertThat(id.value()).isEqualTo(100L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("null 값이면 예외를 던진다")
        void shouldThrowWhenValueIsNull() {
            // When & Then
            assertThatThrownBy(() -> CrawledRawId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("0 이하 값이면 예외를 던진다")
        void shouldThrowWhenValueIsZeroOrNegative(Long value) {
            // When & Then
            assertThatThrownBy(() -> CrawledRawId.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }
    }

    @Nested
    @DisplayName("isNew 메서드")
    class IsNewMethod {

        @Test
        @DisplayName("값이 있으면 false를 반환한다")
        void shouldReturnFalseWhenValueExists() {
            // Given
            CrawledRawId id = CrawledRawId.of(1L);

            // When & Then
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("값이 null이면 true를 반환한다")
        void shouldReturnTrueWhenValueIsNull() {
            // Given
            CrawledRawId id = CrawledRawId.forNew();

            // When & Then
            assertThat(id.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 값을 가진 ID는 동등하다")
        void shouldBeEqualForSameValue() {
            // Given
            CrawledRawId id1 = CrawledRawId.of(1L);
            CrawledRawId id2 = CrawledRawId.of(1L);

            // When & Then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ID는 동등하지 않다")
        void shouldNotBeEqualForDifferentValue() {
            // Given
            CrawledRawId id1 = CrawledRawId.of(1L);
            CrawledRawId id2 = CrawledRawId.of(2L);

            // When & Then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("미할당 ID끼리는 동등하다")
        void shouldBeEqualForUnassignedIds() {
            // Given
            CrawledRawId id1 = CrawledRawId.forNew();
            CrawledRawId id2 = CrawledRawId.forNew();

            // When & Then
            assertThat(id1).isEqualTo(id2);
        }
    }
}
