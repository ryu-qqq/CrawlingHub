package com.ryuqq.crawlinghub.application.useragent.dto.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * BorrowedUserAgent 단위 테스트
 *
 * <p>borrow() 결과 DTO 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("BorrowedUserAgent 테스트")
class BorrowedUserAgentTest {

    @Nested
    @DisplayName("from() 팩토리 메서드 테스트")
    class From {

        @Test
        @DisplayName("[성공] CachedUserAgent로부터 BorrowedUserAgent 생성")
        void shouldCreateFromCachedUserAgent() {
            // Given
            CachedUserAgent cached =
                    new CachedUserAgent(
                            1L,
                            "Mozilla/5.0",
                            "session-token",
                            "nid-value",
                            "uid-value",
                            Instant.now().plusSeconds(3600),
                            80,
                            80,
                            null,
                            null,
                            100,
                            UserAgentStatus.BORROWED,
                            null,
                            Instant.now(),
                            null,
                            2);

            // When
            BorrowedUserAgent borrowed = BorrowedUserAgent.from(cached);

            // Then
            assertThat(borrowed.userAgentId()).isEqualTo(1L);
            assertThat(borrowed.userAgentValue()).isEqualTo("Mozilla/5.0");
            assertThat(borrowed.sessionToken()).isEqualTo("session-token");
            assertThat(borrowed.nid()).isEqualTo("nid-value");
            assertThat(borrowed.mustitUid()).isEqualTo("uid-value");
            assertThat(borrowed.consecutiveRateLimits()).isEqualTo(2);
        }

        @Test
        @DisplayName("[성공] 세션 없는 CachedUserAgent로부터 생성 시 null 유지")
        void shouldPreserveNullsFromCachedUserAgent() {
            // Given
            CachedUserAgent cached =
                    new CachedUserAgent(
                            2L,
                            "Chrome/100",
                            null,
                            null,
                            null,
                            null,
                            80,
                            80,
                            null,
                            null,
                            100,
                            UserAgentStatus.BORROWED,
                            null,
                            Instant.now(),
                            null,
                            0);

            // When
            BorrowedUserAgent borrowed = BorrowedUserAgent.from(cached);

            // Then
            assertThat(borrowed.sessionToken()).isNull();
            assertThat(borrowed.nid()).isNull();
            assertThat(borrowed.mustitUid()).isNull();
            assertThat(borrowed.consecutiveRateLimits()).isZero();
        }
    }

    @Nested
    @DisplayName("hasSearchCookies() 메서드 테스트")
    class HasSearchCookies {

        @Test
        @DisplayName("[성공] nid와 mustitUid 모두 있으면 true")
        void shouldReturnTrueWhenBothCookiesPresent() {
            // Given
            BorrowedUserAgent borrowed =
                    new BorrowedUserAgent(1L, "ua", "token", "nid-val", "uid-val", 0);

            // When / Then
            assertThat(borrowed.hasSearchCookies()).isTrue();
        }

        @Test
        @DisplayName("[실패] nid가 없으면 false")
        void shouldReturnFalseWhenNidMissing() {
            // Given
            BorrowedUserAgent borrowed =
                    new BorrowedUserAgent(1L, "ua", "token", null, "uid-val", 0);

            // When / Then
            assertThat(borrowed.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("[실패] mustitUid가 없으면 false")
        void shouldReturnFalseWhenMustitUidMissing() {
            // Given
            BorrowedUserAgent borrowed =
                    new BorrowedUserAgent(1L, "ua", "token", "nid-val", null, 0);

            // When / Then
            assertThat(borrowed.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("[실패] nid가 빈 문자열이면 false")
        void shouldReturnFalseWhenNidBlank() {
            // Given
            BorrowedUserAgent borrowed =
                    new BorrowedUserAgent(1L, "ua", "token", "  ", "uid-val", 0);

            // When / Then
            assertThat(borrowed.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("[실패] mustitUid가 빈 문자열이면 false")
        void shouldReturnFalseWhenMustitUidBlank() {
            // Given
            BorrowedUserAgent borrowed = new BorrowedUserAgent(1L, "ua", "token", "nid-val", "", 0);

            // When / Then
            assertThat(borrowed.hasSearchCookies()).isFalse();
        }
    }
}
