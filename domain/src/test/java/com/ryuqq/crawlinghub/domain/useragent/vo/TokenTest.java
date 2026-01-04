package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Token Value Object 단위 테스트
 *
 * <p>테스트 대상:
 *
 * <ul>
 *   <li>of() - 암호화된 토큰 생성
 *   <li>empty() - 빈 토큰 생성 (Lazy Token Issuance)
 *   <li>ofNullable() - nullable 값으로 토큰 생성
 *   <li>isEmpty() / isPresent() - 토큰 존재 여부 확인
 *   <li>검증 - Base64 형식, 최소 길이
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("Token VO 테스트")
class TokenTest {

    private static final String VALID_TOKEN =
            "dGhpc0lzQVZhbGlkQmFzZTY0VG9rZW5Gb3JUZXN0aW5nUHVycG9zZXM=";
    private static final String SHORT_TOKEN = "abc"; // 44자 미만
    // 44자 이상이면서 유효하지 않은 Base64 형식 (특수문자 포함)
    private static final String INVALID_BASE64 = "invalid!@#$%^&*()token!@#$%^&*()invalid!!!!extra";

    @Nested
    @DisplayName("of() 토큰 생성 테스트")
    class Of {

        @Test
        @DisplayName("유효한 토큰으로 생성 성공")
        void shouldCreateTokenWithValidValue() {
            // when
            Token token = Token.of(VALID_TOKEN);

            // then
            assertThat(token).isNotNull();
            assertThat(token.encryptedValue()).isEqualTo(VALID_TOKEN);
            assertThat(token.isPresent()).isTrue();
            assertThat(token.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("null 값으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenNull() {
            // when & then
            assertThatThrownBy(() -> Token.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token.of()는 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 예외 발생")
        void shouldThrowExceptionWhenEmpty() {
            // when & then
            assertThatThrownBy(() -> Token.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token.of()는 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("공백 문자열로 생성 시 예외 발생")
        void shouldThrowExceptionWhenBlank() {
            // when & then
            assertThatThrownBy(() -> Token.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token.of()는 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("44자 미만 토큰으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenTooShort() {
            // when & then
            assertThatThrownBy(() -> Token.of(SHORT_TOKEN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token 길이는 최소 44자 이상이어야 합니다");
        }

        @Test
        @DisplayName("유효하지 않은 Base64 형식으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenInvalidBase64() {
            // when & then
            assertThatThrownBy(() -> Token.of(INVALID_BASE64))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token은 유효한 Base64 형식이어야 합니다");
        }
    }

    @Nested
    @DisplayName("empty() 빈 토큰 생성 테스트")
    class Empty {

        @Test
        @DisplayName("빈 토큰 생성 성공")
        void shouldCreateEmptyToken() {
            // when
            Token token = Token.empty();

            // then
            assertThat(token).isNotNull();
            assertThat(token.encryptedValue()).isNull();
            assertThat(token.isEmpty()).isTrue();
            assertThat(token.isPresent()).isFalse();
        }

        @Test
        @DisplayName("빈 토큰은 싱글톤")
        void shouldReturnSameInstance() {
            // when
            Token token1 = Token.empty();
            Token token2 = Token.empty();

            // then
            assertThat(token1).isSameAs(token2);
        }
    }

    @Nested
    @DisplayName("ofNullable() nullable 토큰 생성 테스트")
    class OfNullable {

        @Test
        @DisplayName("유효한 값으로 생성 성공")
        void shouldCreateTokenWithValidValue() {
            // when
            Token token = Token.ofNullable(VALID_TOKEN);

            // then
            assertThat(token).isNotNull();
            assertThat(token.encryptedValue()).isEqualTo(VALID_TOKEN);
            assertThat(token.isPresent()).isTrue();
        }

        @Test
        @DisplayName("null 값으로 생성 시 빈 토큰 반환")
        void shouldReturnEmptyTokenWhenNull() {
            // when
            Token token = Token.ofNullable(null);

            // then
            assertThat(token).isNotNull();
            assertThat(token.isEmpty()).isTrue();
            assertThat(token).isEqualTo(Token.empty());
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 빈 토큰 반환")
        void shouldReturnEmptyTokenWhenEmpty() {
            // when
            Token token = Token.ofNullable("");

            // then
            assertThat(token).isNotNull();
            assertThat(token.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("공백 문자열로 생성 시 빈 토큰 반환")
        void shouldReturnEmptyTokenWhenBlank() {
            // when
            Token token = Token.ofNullable("   ");

            // then
            assertThat(token).isNotNull();
            assertThat(token.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("isEmpty() / isPresent() 상태 확인 테스트")
    class StateCheck {

        @Test
        @DisplayName("유효한 토큰은 isPresent() == true, isEmpty() == false")
        void shouldReturnCorrectStateForValidToken() {
            // given
            Token token = Token.of(VALID_TOKEN);

            // then
            assertThat(token.isPresent()).isTrue();
            assertThat(token.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("빈 토큰은 isEmpty() == true, isPresent() == false")
        void shouldReturnCorrectStateForEmptyToken() {
            // given
            Token token = Token.empty();

            // then
            assertThat(token.isEmpty()).isTrue();
            assertThat(token.isPresent()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals() / hashCode() 테스트")
    class EqualsAndHashCode {

        @Test
        @DisplayName("같은 값의 토큰은 동일")
        void shouldBeEqualForSameValue() {
            // given
            Token token1 = Token.of(VALID_TOKEN);
            Token token2 = Token.of(VALID_TOKEN);

            // then
            assertThat(token1).isEqualTo(token2);
            assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 토큰은 다름")
        void shouldNotBeEqualForDifferentValue() {
            // given
            Token token1 = Token.of(VALID_TOKEN);
            Token token2 = Token.of("YW5vdGhlclZhbGlkQmFzZTY0VG9rZW5Gb3JUZXN0aW5nT25seQ==");

            // then
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("빈 토큰끼리는 동일")
        void shouldBeEqualForEmptyTokens() {
            // given
            Token token1 = Token.empty();
            Token token2 = Token.ofNullable(null);

            // then
            assertThat(token1).isEqualTo(token2);
        }
    }
}
