package com.ryuqq.crawlinghub.domain.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Token Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("Token Value Object 단위 테스트")
class TokenTest {

    private static final LocalDateTime ISSUED_AT = LocalDateTime.of(2025, 11, 7, 12, 0, 0);
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2025, 11, 7, 13, 0, 0);  // 1시간 후

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("유효한 토큰 정보로 Token 생성 성공")
        void shouldCreateWithValidTokenInfo() {
            // Given
            String tokenValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

            // When
            Token token = Token.of(tokenValue, ISSUED_AT, EXPIRES_AT);

            // Then
            assertThat(token).isNotNull();
            assertThat(token.getValue()).isEqualTo(tokenValue);
            assertThat(token.getIssuedAt()).isEqualTo(ISSUED_AT);
            assertThat(token.getExpiresAt()).isEqualTo(EXPIRES_AT);
        }

        @Test
        @DisplayName("짧은 토큰 값으로도 생성 성공")
        void shouldCreateWithShortToken() {
            // Given
            String shortToken = "abc123";

            // When
            Token token = Token.of(shortToken, ISSUED_AT, EXPIRES_AT);

            // Then
            assertThat(token).isNotNull();
            assertThat(token.getValue()).isEqualTo(shortToken);
        }

        @Test
        @DisplayName("긴 만료 시간으로 생성 성공")
        void shouldCreateWithLongExpiration() {
            // Given
            String tokenValue = "token123";
            LocalDateTime longExpiresAt = ISSUED_AT.plusDays(30);  // 30일 후

            // When
            Token token = Token.of(tokenValue, ISSUED_AT, longExpiresAt);

            // Then
            assertThat(token.getExpiresAt()).isEqualTo(longExpiresAt);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @NullSource
        @DisplayName("null 토큰 값으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenNullToken(String nullToken) {
            // When & Then
            assertThatThrownBy(() -> Token.of(nullToken, ISSUED_AT, EXPIRES_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("토큰 값은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
        @DisplayName("빈 문자열이나 공백 토큰으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenBlankToken(String blankToken) {
            // When & Then
            assertThatThrownBy(() -> Token.of(blankToken, ISSUED_AT, EXPIRES_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("토큰 값은 필수입니다");
        }

        @Test
        @DisplayName("null 발급 시간으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenNullIssuedAt() {
            // When & Then
            assertThatThrownBy(() -> Token.of("token123", null, EXPIRES_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("발급 시간은 필수입니다");
        }

        @Test
        @DisplayName("null 만료 시간으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenNullExpiresAt() {
            // When & Then
            assertThatThrownBy(() -> Token.of("token123", ISSUED_AT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료 시간은 필수입니다");
        }

        @Test
        @DisplayName("만료 시간이 발급 시간보다 이전이면 예외 발생")
        void shouldThrowExceptionWhenExpiresAtBeforeIssuedAt() {
            // Given
            LocalDateTime invalidExpiresAt = ISSUED_AT.minusHours(1);  // 발급 시간보다 이전

            // When & Then
            assertThatThrownBy(() -> Token.of("token123", ISSUED_AT, invalidExpiresAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료 시간은 발급 시간 이후여야 합니다");
        }

        @Test
        @DisplayName("만료 시간이 발급 시간과 같으면 유효 (경계값)")
        void shouldBeValidWhenExpiresAtEqualsIssuedAt() {
            // Given
            LocalDateTime sameTime = ISSUED_AT;

            // When
            Token token = Token.of("token123", ISSUED_AT, sameTime);

            // Then
            assertThat(token).isNotNull();
            assertThat(token.getIssuedAt()).isEqualTo(token.getExpiresAt());
        }
    }

    @Nested
    @DisplayName("토큰 만료 검증 테스트")
    class ExpirationTests {

        @Test
        @DisplayName("isExpired() - 현재 시간이 만료 시간 이전이면 false 반환")
        void shouldReturnFalseWhenNotExpired() {
            // Given
            Token token = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            LocalDateTime now = EXPIRES_AT.minusMinutes(30);  // 만료 30분 전

            // When
            boolean expired = token.isExpired(now);

            // Then
            assertThat(expired).isFalse();
        }

        @Test
        @DisplayName("isExpired() - 현재 시간이 만료 시간 이후면 true 반환")
        void shouldReturnTrueWhenExpired() {
            // Given
            Token token = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            LocalDateTime now = EXPIRES_AT.plusMinutes(1);  // 만료 1분 후

            // When
            boolean expired = token.isExpired(now);

            // Then
            assertThat(expired).isTrue();
        }

        @Test
        @DisplayName("isExpired() - 현재 시간이 정확히 만료 시간이면 false 반환 (경계값)")
        void shouldReturnFalseWhenExactlyExpired() {
            // Given
            Token token = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            LocalDateTime now = EXPIRES_AT;  // 정확히 만료 시간

            // When
            boolean expired = token.isExpired(now);

            // Then
            assertThat(expired).isFalse();  // isAfter이므로 false
        }

        @Test
        @DisplayName("isValid() - 만료되지 않았으면 true 반환")
        void shouldReturnTrueWhenValid() {
            // Given
            Token token = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            LocalDateTime now = EXPIRES_AT.minusMinutes(30);

            // When
            boolean valid = token.isValid(now);

            // Then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("isValid() - 만료되었으면 false 반환")
        void shouldReturnFalseWhenInvalid() {
            // Given
            Token token = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            LocalDateTime now = EXPIRES_AT.plusMinutes(1);

            // When
            boolean valid = token.isValid(now);

            // Then
            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("남은 시간 계산 테스트")
    class RemainingTimeTests {

        @Test
        @DisplayName("remainingSeconds() - 만료까지 30분 남았을 때 정확한 초 반환")
        void shouldReturnCorrectRemainingSeconds() {
            // Given
            Token token = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            LocalDateTime now = EXPIRES_AT.minusMinutes(30);  // 30분 전

            // When
            long remainingSeconds = token.remainingSeconds(now);

            // Then
            assertThat(remainingSeconds).isEqualTo(30 * 60);  // 1800초
        }

        @Test
        @DisplayName("remainingSeconds() - 만료 1초 전일 때 1초 반환")
        void shouldReturnOneSecondWhenOneSecondLeft() {
            // Given
            Token token = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            LocalDateTime now = EXPIRES_AT.minusSeconds(1);

            // When
            long remainingSeconds = token.remainingSeconds(now);

            // Then
            assertThat(remainingSeconds).isEqualTo(1);
        }

        @Test
        @DisplayName("remainingSeconds() - 이미 만료되었으면 음수 반환")
        void shouldReturnNegativeWhenExpired() {
            // Given
            Token token = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            LocalDateTime now = EXPIRES_AT.plusMinutes(10);  // 만료 10분 후

            // When
            long remainingSeconds = token.remainingSeconds(now);

            // Then
            assertThat(remainingSeconds).isNegative();
            assertThat(remainingSeconds).isEqualTo(-10 * 60);  // -600초
        }

        @Test
        @DisplayName("remainingSeconds() - 정확히 만료 시간이면 0 반환")
        void shouldReturnZeroWhenExactlyExpired() {
            // Given
            Token token = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            LocalDateTime now = EXPIRES_AT;

            // When
            long remainingSeconds = token.remainingSeconds(now);

            // Then
            assertThat(remainingSeconds).isZero();
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 토큰 정보를 가진 두 Token은 같다")
        void shouldBeEqualForSameTokenInfo() {
            // Given
            String tokenValue = "token123";
            Token token1 = Token.of(tokenValue, ISSUED_AT, EXPIRES_AT);
            Token token2 = Token.of(tokenValue, ISSUED_AT, EXPIRES_AT);

            // When & Then
            assertThat(token1).isEqualTo(token2);
        }

        @Test
        @DisplayName("다른 토큰 값을 가진 두 Token은 다르다")
        void shouldNotBeEqualForDifferentValue() {
            // Given
            Token token1 = Token.of("token123", ISSUED_AT, EXPIRES_AT);
            Token token2 = Token.of("token456", ISSUED_AT, EXPIRES_AT);

            // When & Then
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("같은 토큰 정보를 가진 두 Token은 같은 hashCode를 반환한다")
        void shouldReturnSameHashCodeForSameTokenInfo() {
            // Given
            String tokenValue = "token123";
            Token token1 = Token.of(tokenValue, ISSUED_AT, EXPIRES_AT);
            Token token2 = Token.of(tokenValue, ISSUED_AT, EXPIRES_AT);

            // When & Then
            assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 긴 토큰을 부분 마스킹한다 (앞 4자리 + ... + 뒤 4자리)")
        void shouldPartiallyMaskLongToken() {
            // Given
            String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
            Token token = Token.of(longToken, ISSUED_AT, EXPIRES_AT);

            // When
            String result = token.toString();

            // Then
            // Token{value='eyJh...VCJ9', issuedAt=..., expiresAt=...} 형식
            assertThat(result).contains("Token{");
            assertThat(result).contains("value='eyJh...VCJ9'");
            assertThat(result).contains("issuedAt=" + ISSUED_AT);
            assertThat(result).contains("expiresAt=" + EXPIRES_AT);
        }

        @Test
        @DisplayName("toString()은 짧은 토큰 (<8자)을 완전 마스킹한다")
        void shouldFullyMaskShortToken() {
            // Given
            String shortToken = "abc123";  // 6자
            Token token = Token.of(shortToken, ISSUED_AT, EXPIRES_AT);

            // When
            String result = token.toString();

            // Then
            assertThat(result).contains("***");
            assertThat(result).doesNotContain("abc123");
        }

        @Test
        @DisplayName("toString()은 토큰 원본 값을 노출하지 않는다")
        void shouldNotExposeOriginalToken() {
            // Given
            String sensitiveToken = "supersecrettoken12345";
            Token token = Token.of(sensitiveToken, ISSUED_AT, EXPIRES_AT);

            // When
            String result = token.toString();

            // Then
            assertThat(result).doesNotContain(sensitiveToken);
        }
    }
}
