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
@DisplayName("Email Value Object 단위 테스트")
class EmailTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 이메일로 생성한다")
        void createWithValidEmail() {
            // when
            Email email = Email.of("user@example.com");

            // then
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("대문자 이메일을 소문자로 변환한다")
        void convertToLowercase() {
            // when
            Email email = Email.of("USER@EXAMPLE.COM");

            // then
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("앞뒤 공백을 제거하고 소문자로 변환한다")
        void trimAndConvertToLowercase() {
            // when
            Email email = Email.of("  User@Example.Com  ");

            // then
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("이메일이 null이면 예외가 발생한다")
        void throwWhenNull() {
            assertThatThrownBy(() -> Email.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일");
        }

        @Test
        @DisplayName("이메일이 빈 문자열이면 예외가 발생한다")
        void throwWhenBlank() {
            assertThatThrownBy(() -> Email.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "@domain.com", "user@", "user@domain", "user@.com"})
        @DisplayName("유효하지 않은 이메일 형식이면 예외가 발생한다")
        void throwWhenInvalidFormat(String invalidEmail) {
            assertThatThrownBy(() -> Email.of(invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 이메일");
        }
    }

    @Nested
    @DisplayName("localPart() 메서드 테스트")
    class LocalPartTest {

        @Test
        @DisplayName("@ 앞부분을 반환한다")
        void returnLocalPart() {
            // given
            Email email = Email.of("user@example.com");

            // then
            assertThat(email.localPart()).isEqualTo("user");
        }
    }

    @Nested
    @DisplayName("domainPart() 메서드 테스트")
    class DomainPartTest {

        @Test
        @DisplayName("@ 뒷부분을 반환한다")
        void returnDomainPart() {
            // given
            Email email = Email.of("user@example.com");

            // then
            assertThat(email.domainPart()).isEqualTo("example.com");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 이메일이면 동일하다")
        void sameEmailAreEqual() {
            // given
            Email email1 = Email.of("user@example.com");
            Email email2 = Email.of("user@example.com");

            // then
            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("대소문자 차이는 동일하다")
        void caseInsensitiveEquality() {
            // given
            Email email1 = Email.of("User@Example.com");
            Email email2 = Email.of("user@example.com");

            // then
            assertThat(email1).isEqualTo(email2);
        }

        @Test
        @DisplayName("다른 이메일이면 다르다")
        void differentEmailAreNotEqual() {
            // given
            Email email1 = Email.of("user1@example.com");
            Email email2 = Email.of("user2@example.com");

            // then
            assertThat(email1).isNotEqualTo(email2);
        }
    }
}
