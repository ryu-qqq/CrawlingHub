package com.ryuqq.crawlinghub.domain.common.vo;

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
@Tag("vo")
@DisplayName("PhoneNumber Value Object 단위 테스트")
class PhoneNumberTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 전화번호 (하이픈 포함)로 생성한다")
        void createWithHyphen() {
            // when
            PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");

            // then
            assertThat(phoneNumber.value()).isEqualTo("010-1234-5678");
        }

        @Test
        @DisplayName("유효한 전화번호 (숫자만)로 생성한다")
        void createWithDigitsOnly() {
            // when
            PhoneNumber phoneNumber = PhoneNumber.of("01012345678");

            // then
            assertThat(phoneNumber.value()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("앞뒤 공백을 제거하여 생성한다")
        void trimWhitespace() {
            // when
            PhoneNumber phoneNumber = PhoneNumber.of("  010-1234-5678  ");

            // then
            assertThat(phoneNumber.value()).isEqualTo("010-1234-5678");
        }

        @Test
        @DisplayName("전화번호가 null이면 예외가 발생한다")
        void throwWhenNull() {
            assertThatThrownBy(() -> PhoneNumber.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전화번호");
        }

        @Test
        @DisplayName("전화번호가 빈 문자열이면 예외가 발생한다")
        void throwWhenBlank() {
            assertThatThrownBy(() -> PhoneNumber.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전화번호");
        }

        @ParameterizedTest
        @ValueSource(strings = {"12", "abc-1234-5678", "010-abc-5678", "010/1234/5678"})
        @DisplayName("유효하지 않은 전화번호 형식이면 예외가 발생한다")
        void throwWhenInvalidFormat(String invalidPhone) {
            assertThatThrownBy(() -> PhoneNumber.of(invalidPhone))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 전화번호");
        }
    }

    @Nested
    @DisplayName("digitsOnly() 메서드 테스트")
    class DigitsOnlyTest {

        @Test
        @DisplayName("하이픈을 제거하고 숫자만 반환한다")
        void removeHyphens() {
            // given
            PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");

            // then
            assertThat(phoneNumber.digitsOnly()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("하이픈이 없으면 그대로 반환한다")
        void returnAsIsWhenNoHyphen() {
            // given
            PhoneNumber phoneNumber = PhoneNumber.of("01012345678");

            // then
            assertThat(phoneNumber.digitsOnly()).isEqualTo("01012345678");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 전화번호이면 동일하다")
        void samePhoneNumberAreEqual() {
            // given
            PhoneNumber phone1 = PhoneNumber.of("010-1234-5678");
            PhoneNumber phone2 = PhoneNumber.of("010-1234-5678");

            // then
            assertThat(phone1).isEqualTo(phone2);
            assertThat(phone1.hashCode()).isEqualTo(phone2.hashCode());
        }

        @Test
        @DisplayName("다른 전화번호이면 다르다")
        void differentPhoneNumberAreNotEqual() {
            // given
            PhoneNumber phone1 = PhoneNumber.of("010-1234-5678");
            PhoneNumber phone2 = PhoneNumber.of("010-9876-5432");

            // then
            assertThat(phone1).isNotEqualTo(phone2);
        }
    }
}
