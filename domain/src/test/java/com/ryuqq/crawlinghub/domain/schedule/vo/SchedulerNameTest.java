package com.ryuqq.crawlinghub.domain.schedule.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("SchedulerName Value Object 단위 테스트")
class SchedulerNameTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("유효한 이름으로 생성한다")
        void createWithValidName() {
            SchedulerName name = SchedulerName.of("테스트 스케줄러");
            assertThat(name.value()).isEqualTo("테스트 스케줄러");
        }

        @Test
        @DisplayName("공백이 포함된 이름은 trim되어 저장된다")
        void trimsWhitespace() {
            SchedulerName name = SchedulerName.of("  테스트  ");
            assertThat(name.value()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("정확히 50자인 이름으로 생성한다")
        void createWithExactlyMaxLength() {
            String maxName = "A".repeat(50);
            SchedulerName name = SchedulerName.of(maxName);
            assertThat(name.value()).hasSize(50);
        }
    }

    @Nested
    @DisplayName("생성 실패 테스트 - 검증")
    class ValidationTest {

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void nullThrowsException() {
            assertThatThrownBy(() -> SchedulerName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void blankThrowsException() {
            assertThatThrownBy(() -> SchedulerName.of("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("51자이면 예외가 발생한다")
        void tooLongThrowsException() {
            String tooLong = "A".repeat(51);
            assertThatThrownBy(() -> SchedulerName.of(tooLong))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("50");
        }
    }

    @Nested
    @DisplayName("isSameAs() 테스트")
    class IsSameAsTest {

        @Test
        @DisplayName("같은 이름이면 true를 반환한다")
        void sameNameReturnsTrue() {
            SchedulerName name = SchedulerName.of("스케줄러");
            assertThat(name.isSameAs("스케줄러")).isTrue();
        }

        @Test
        @DisplayName("다른 이름이면 false를 반환한다")
        void differentNameReturnsFalse() {
            SchedulerName name = SchedulerName.of("스케줄러");
            assertThat(name.isSameAs("다른스케줄러")).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValueAreEqual() {
            SchedulerName name1 = SchedulerName.of("스케줄러");
            SchedulerName name2 = SchedulerName.of("스케줄러");
            assertThat(name1).isEqualTo(name2);
            assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 다르다")
        void differentValuesAreNotEqual() {
            SchedulerName name1 = SchedulerName.of("스케줄러A");
            SchedulerName name2 = SchedulerName.of("스케줄러B");
            assertThat(name1).isNotEqualTo(name2);
        }
    }
}
