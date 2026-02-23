package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("UserAgentString Value Object 단위 테스트")
class UserAgentStringTest {

    private static final String VALID_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/91.0";

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 값으로 생성한다")
        void createWithValidValue() {
            UserAgentString ua = new UserAgentString(VALID_UA);
            assertThat(ua.value()).isEqualTo(VALID_UA);
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void nullValueThrowsException() {
            assertThatThrownBy(() -> new UserAgentString(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void blankValueThrowsException() {
            assertThatThrownBy(() -> new UserAgentString("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("10자 미만이면 예외가 발생한다")
        void tooShortThrowsException() {
            assertThatThrownBy(() -> new UserAgentString("short"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최소");
        }

        @Test
        @DisplayName("501자 이상이면 예외가 발생한다")
        void tooLongThrowsException() {
            String tooLong = "A".repeat(501);
            assertThatThrownBy(() -> new UserAgentString(tooLong))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최대");
        }

        @Test
        @DisplayName("정확히 500자이면 생성된다")
        void exactlyMaxLengthIsValid() {
            String maxLength = "Mozilla/5.0 " + "A".repeat(488);
            UserAgentString ua = new UserAgentString(maxLength);
            assertThat(ua.value()).hasSize(500);
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("유효한 값으로 생성한다")
        void createWithValidValue() {
            UserAgentString ua = UserAgentString.of(VALID_UA);
            assertThat(ua.value()).isEqualTo(VALID_UA);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValueAreEqual() {
            UserAgentString ua1 = UserAgentString.of(VALID_UA);
            UserAgentString ua2 = UserAgentString.of(VALID_UA);
            assertThat(ua1).isEqualTo(ua2);
            assertThat(ua1.hashCode()).isEqualTo(ua2.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 다르다")
        void differentValuesAreNotEqual() {
            UserAgentString ua1 = UserAgentString.of(VALID_UA);
            UserAgentString ua2 = UserAgentString.of("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)");
            assertThat(ua1).isNotEqualTo(ua2);
        }
    }
}
