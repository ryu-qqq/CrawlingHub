package com.ryuqq.crawlinghub.domain.task.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("id")
@DisplayName("TargetId Value Object 단위 테스트")
class TargetIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("양수 값으로 생성한다")
        void createWithPositiveValue() {
            // when
            TargetId targetId = TargetId.of(12345L);

            // then
            assertThat(targetId.value()).isEqualTo(12345L);
            assertThat(targetId.hasTarget()).isTrue();
        }

        @Test
        @DisplayName("empty()로 null 값을 가진 TargetId를 생성한다")
        void createEmpty() {
            // when
            TargetId targetId = TargetId.empty();

            // then
            assertThat(targetId.value()).isNull();
            assertThat(targetId.hasTarget()).isFalse();
        }

        @Test
        @DisplayName("null 값으로 생성하면 대상 없음이다")
        void createWithNullValue() {
            // when
            TargetId targetId = TargetId.of(null);

            // then
            assertThat(targetId.value()).isNull();
            assertThat(targetId.hasTarget()).isFalse();
        }

        @Test
        @DisplayName("0 값으로 생성하면 예외가 발생한다")
        void throwWhenZero() {
            assertThatThrownBy(() -> TargetId.of(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }

        @Test
        @DisplayName("음수 값으로 생성하면 예외가 발생한다")
        void throwWhenNegative() {
            assertThatThrownBy(() -> TargetId.of(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValueAreEqual() {
            // given
            TargetId id1 = TargetId.of(100L);
            TargetId id2 = TargetId.of(100L);

            // then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("empty와 null로 생성한 TargetId는 동일하다")
        void emptyEqualsNullCreated() {
            // given
            TargetId id1 = TargetId.empty();
            TargetId id2 = TargetId.of(null);

            // then
            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("다른 값이면 다르다")
        void differentValueAreNotEqual() {
            // given
            TargetId id1 = TargetId.of(100L);
            TargetId id2 = TargetId.of(200L);

            // then
            assertThat(id1).isNotEqualTo(id2);
        }
    }
}
