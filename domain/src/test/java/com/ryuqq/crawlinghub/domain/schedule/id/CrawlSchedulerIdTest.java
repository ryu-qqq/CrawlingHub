package com.ryuqq.crawlinghub.domain.schedule.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("id")
@DisplayName("CrawlSchedulerId Value Object 단위 테스트")
class CrawlSchedulerIdTest {

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNewTest {

        @Test
        @DisplayName("신규 생성 시 value가 null이다")
        void forNewReturnsNullValue() {
            CrawlSchedulerId id = CrawlSchedulerId.forNew();
            assertThat(id.value()).isNull();
            assertThat(id.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfTest {

        @Test
        @DisplayName("유효한 값으로 생성한다")
        void createWithValidValue() {
            CrawlSchedulerId id = CrawlSchedulerId.of(1L);
            assertThat(id.value()).isEqualTo(1L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void nullValueThrowsException() {
            assertThatThrownBy(() -> CrawlSchedulerId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("0이면 예외가 발생한다")
        void zeroValueThrowsException() {
            assertThatThrownBy(() -> new CrawlSchedulerId(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }

        @Test
        @DisplayName("음수이면 예외가 발생한다")
        void negativeValueThrowsException() {
            assertThatThrownBy(() -> new CrawlSchedulerId(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }
    }

    @Nested
    @DisplayName("isNew() 테스트")
    class IsNewTest {

        @Test
        @DisplayName("value가 null이면 신규이다")
        void nullValueIsNew() {
            CrawlSchedulerId id = CrawlSchedulerId.forNew();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("value가 있으면 신규가 아니다")
        void existingValueIsNotNew() {
            CrawlSchedulerId id = CrawlSchedulerId.of(10L);
            assertThat(id.isNew()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValueAreEqual() {
            CrawlSchedulerId id1 = CrawlSchedulerId.of(1L);
            CrawlSchedulerId id2 = CrawlSchedulerId.of(1L);
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 다르다")
        void differentValuesAreNotEqual() {
            CrawlSchedulerId id1 = CrawlSchedulerId.of(1L);
            CrawlSchedulerId id2 = CrawlSchedulerId.of(2L);
            assertThat(id1).isNotEqualTo(id2);
        }
    }
}
