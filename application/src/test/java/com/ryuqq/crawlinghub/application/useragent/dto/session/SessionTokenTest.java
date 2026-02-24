package com.ryuqq.crawlinghub.application.useragent.dto.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SessionToken 단위 테스트
 *
 * <p>세션 토큰 유효성 검증 로직 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SessionToken 테스트")
class SessionTokenTest {

    @Nested
    @DisplayName("isValid() 테스트")
    class IsValid {

        @Test
        @DisplayName("[성공] 만료 전 세션은 유효")
        void shouldReturnTrueWhenSessionNotExpired() {
            // Given
            Instant future = Instant.now().plusSeconds(3600);
            SessionToken token = new SessionToken("token-value", "nid-value", "uid-value", future);

            // When
            boolean valid = token.isValid(Instant.now());

            // Then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("[실패] 만료된 세션은 무효")
        void shouldReturnFalseWhenSessionExpired() {
            // Given
            Instant past = Instant.now().minusSeconds(3600);
            SessionToken token = new SessionToken("token-value", "nid-value", "uid-value", past);

            // When
            boolean valid = token.isValid(Instant.now());

            // Then
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("[실패] expiresAt가 null이면 무효")
        void shouldReturnFalseWhenExpiresAtIsNull() {
            // Given
            SessionToken token = new SessionToken("token-value", "nid-value", "uid-value", null);

            // When
            boolean valid = token.isValid(Instant.now());

            // Then
            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("isExpired() 테스트")
    class IsExpired {

        @Test
        @DisplayName("[성공] 만료된 세션은 isExpired=true")
        void shouldReturnTrueWhenSessionExpired() {
            // Given
            Instant past = Instant.now().minusSeconds(3600);
            SessionToken token = new SessionToken("token-value", "nid-value", "uid-value", past);

            // When
            boolean expired = token.isExpired(Instant.now());

            // Then
            assertThat(expired).isTrue();
        }

        @Test
        @DisplayName("[성공] 만료 전 세션은 isExpired=false")
        void shouldReturnFalseWhenSessionNotExpired() {
            // Given
            Instant future = Instant.now().plusSeconds(3600);
            SessionToken token = new SessionToken("token-value", "nid-value", "uid-value", future);

            // When
            boolean expired = token.isExpired(Instant.now());

            // Then
            assertThat(expired).isFalse();
        }

        @Test
        @DisplayName("[실패] expiresAt가 null이면 isExpired=false")
        void shouldReturnFalseWhenExpiresAtIsNull() {
            // Given
            SessionToken token = new SessionToken("token-value", "nid-value", "uid-value", null);

            // When
            boolean expired = token.isExpired(Instant.now());

            // Then
            assertThat(expired).isFalse();
        }
    }

    @Nested
    @DisplayName("hasSearchCookies() 테스트")
    class HasSearchCookies {

        @Test
        @DisplayName("[성공] nid와 mustitUid가 모두 있으면 true")
        void shouldReturnTrueWhenBothCookiesPresent() {
            // Given
            SessionToken token =
                    new SessionToken(
                            "token-value",
                            "nid-value",
                            "uid-value",
                            Instant.now().plusSeconds(3600));

            // When
            boolean hasCookies = token.hasSearchCookies();

            // Then
            assertThat(hasCookies).isTrue();
        }

        @Test
        @DisplayName("[실패] nid가 null이면 false")
        void shouldReturnFalseWhenNidIsNull() {
            // Given
            SessionToken token =
                    new SessionToken(
                            "token-value", null, "uid-value", Instant.now().plusSeconds(3600));

            // When
            boolean hasCookies = token.hasSearchCookies();

            // Then
            assertThat(hasCookies).isFalse();
        }

        @Test
        @DisplayName("[실패] mustitUid가 빈 문자열이면 false")
        void shouldReturnFalseWhenMustitUidIsBlank() {
            // Given
            SessionToken token =
                    new SessionToken(
                            "token-value", "nid-value", "  ", Instant.now().plusSeconds(3600));

            // When
            boolean hasCookies = token.hasSearchCookies();

            // Then
            assertThat(hasCookies).isFalse();
        }

        @Test
        @DisplayName("[실패] nid와 mustitUid 모두 없으면 false")
        void shouldReturnFalseWhenBothCookiesMissing() {
            // Given
            SessionToken token =
                    new SessionToken("token-value", null, null, Instant.now().plusSeconds(3600));

            // When
            boolean hasCookies = token.hasSearchCookies();

            // Then
            assertThat(hasCookies).isFalse();
        }
    }
}
