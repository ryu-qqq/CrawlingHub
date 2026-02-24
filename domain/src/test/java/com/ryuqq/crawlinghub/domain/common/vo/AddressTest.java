package com.ryuqq.crawlinghub.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("Address Value Object 단위 테스트")
class AddressTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 우편번호와 기본 주소로 생성한다")
        void createWithValidValues() {
            // when
            Address address = Address.of("12345", "서울시 강남구 테헤란로 123");

            // then
            assertThat(address.zipcode()).isEqualTo("12345");
            assertThat(address.line1()).isEqualTo("서울시 강남구 테헤란로 123");
            assertThat(address.line2()).isNull();
        }

        @Test
        @DisplayName("상세 주소를 포함하여 생성한다")
        void createWithLine2() {
            // when
            Address address = Address.of("12345", "서울시 강남구 테헤란로 123", "101동 202호");

            // then
            assertThat(address.zipcode()).isEqualTo("12345");
            assertThat(address.line1()).isEqualTo("서울시 강남구 테헤란로 123");
            assertThat(address.line2()).isEqualTo("101동 202호");
        }

        @Test
        @DisplayName("앞뒤 공백을 제거하여 생성한다")
        void trimWhitespace() {
            // when
            Address address = Address.of("  12345  ", "  서울시 강남구  ", "  101동  ");

            // then
            assertThat(address.zipcode()).isEqualTo("12345");
            assertThat(address.line1()).isEqualTo("서울시 강남구");
            assertThat(address.line2()).isEqualTo("101동");
        }

        @Test
        @DisplayName("우편번호가 null이면 예외가 발생한다")
        void throwWhenZipcodeIsNull() {
            assertThatThrownBy(() -> Address.of(null, "서울시 강남구"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("우편번호");
        }

        @Test
        @DisplayName("우편번호가 빈 문자열이면 예외가 발생한다")
        void throwWhenZipcodeIsBlank() {
            assertThatThrownBy(() -> Address.of("   ", "서울시 강남구"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("우편번호");
        }

        @Test
        @DisplayName("기본 주소가 null이면 예외가 발생한다")
        void throwWhenLine1IsNull() {
            assertThatThrownBy(() -> Address.of("12345", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("기본 주소");
        }

        @Test
        @DisplayName("기본 주소가 빈 문자열이면 예외가 발생한다")
        void throwWhenLine1IsBlank() {
            assertThatThrownBy(() -> Address.of("12345", "  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("기본 주소");
        }
    }

    @Nested
    @DisplayName("fullAddress() 메서드 테스트")
    class FullAddressTest {

        @Test
        @DisplayName("line2가 없으면 line1만 반환한다")
        void returnLine1WhenNoLine2() {
            // given
            Address address = Address.of("12345", "서울시 강남구 테헤란로 123");

            // when
            String fullAddress = address.fullAddress();

            // then
            assertThat(fullAddress).isEqualTo("서울시 강남구 테헤란로 123");
        }

        @Test
        @DisplayName("line2가 있으면 line1과 line2를 공백으로 합쳐 반환한다")
        void returnCombinedAddressWhenLine2Present() {
            // given
            Address address = Address.of("12345", "서울시 강남구 테헤란로 123", "101동 202호");

            // when
            String fullAddress = address.fullAddress();

            // then
            assertThat(fullAddress).isEqualTo("서울시 강남구 테헤란로 123 101동 202호");
        }

        @Test
        @DisplayName("line2가 공백이면 line1만 반환한다")
        void returnLine1WhenLine2IsBlank() {
            // given
            Address address = Address.of("12345", "서울시 강남구", "  ");

            // when
            String fullAddress = address.fullAddress();

            // then
            assertThat(fullAddress).isEqualTo("서울시 강남구");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            // given
            Address address1 = Address.of("12345", "서울시 강남구", "101동");
            Address address2 = Address.of("12345", "서울시 강남구", "101동");

            // then
            assertThat(address1).isEqualTo(address2);
            assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
        }

        @Test
        @DisplayName("다른 우편번호이면 다르다")
        void differentZipcodeAreNotEqual() {
            // given
            Address address1 = Address.of("12345", "서울시 강남구");
            Address address2 = Address.of("67890", "서울시 강남구");

            // then
            assertThat(address1).isNotEqualTo(address2);
        }
    }
}
