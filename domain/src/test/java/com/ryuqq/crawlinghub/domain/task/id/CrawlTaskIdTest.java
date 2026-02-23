package com.ryuqq.crawlinghub.domain.task.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CrawlTaskId Value Object 단위 테스트
 *
 * <p>Kent Beck TDD - Red Phase
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskId VO 테스트")
class CrawlTaskIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @ParameterizedTest
        @ValueSource(longs = {1L, 100L, Long.MAX_VALUE})
        @DisplayName("양수 값으로 생성 성공")
        void shouldCreateWithPositiveValue(Long value) {
            // given & when
            CrawlTaskId id = new CrawlTaskId(value);

            // then
            assertThat(id.value()).isEqualTo(value);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L, Long.MIN_VALUE})
        @DisplayName("0 이하 값 생성 시 예외 발생")
        void shouldThrowExceptionForZeroOrNegative(Long value) {
            // given & when & then
            assertThatThrownBy(() -> new CrawlTaskId(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }
    }

    @Nested
    @DisplayName("정적 팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("forNew()는 null 값을 반환")
        void shouldCreateForNewWithNullValue() {
            // given & when
            CrawlTaskId id = CrawlTaskId.forNew();

            // then
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("of() 정적 팩토리로 생성 성공")
        void shouldCreateWithOfMethod() {
            // given
            Long value = 123L;

            // when
            CrawlTaskId id = CrawlTaskId.of(value);

            // then
            assertThat(id.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("of(null) 호출 시 예외 발생")
        void shouldThrowExceptionWhenOfCalledWithNull() {
            // given & when & then
            assertThatThrownBy(() -> CrawlTaskId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("isNew() 메서드 테스트")
    class IsNewTest {

        @Test
        @DisplayName("값이 있으면 isNew()가 false 반환")
        void shouldReturnFalseWhenValueExists() {
            // given
            CrawlTaskId id = CrawlTaskIdFixture.anAssignedId();

            // when
            boolean result = id.isNew();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("값이 null이면 isNew()가 true 반환")
        void shouldReturnTrueWhenValueIsNull() {
            // given
            CrawlTaskId id = CrawlTaskIdFixture.anUnassignedId();

            // when
            boolean result = id.isNew();

            // then
            assertThat(result).isTrue();
        }
    }
}
