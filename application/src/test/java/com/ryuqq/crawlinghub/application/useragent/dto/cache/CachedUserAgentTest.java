package com.ryuqq.crawlinghub.application.useragent.dto.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CachedUserAgent 단위 테스트
 *
 * <p>Redis Pool 저장용 UserAgent DTO 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CachedUserAgent 테스트")
class CachedUserAgentTest {

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNew {

        @Test
        @DisplayName("[성공] SESSION_REQUIRED 상태로 생성됨")
        void shouldCreateWithSessionRequired() {
            // Given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // When
            CachedUserAgent cached = CachedUserAgent.forNew(userAgent);

            // Then
            assertThat(cached.userAgentId()).isEqualTo(userAgent.getIdValue());
            assertThat(cached.userAgentValue()).isEqualTo(userAgent.getUserAgentStringValue());
            assertThat(cached.status()).isEqualTo(UserAgentStatus.SESSION_REQUIRED);
            assertThat(cached.sessionToken()).isNull();
            assertThat(cached.nid()).isNull();
            assertThat(cached.mustitUid()).isNull();
            assertThat(cached.sessionExpiresAt()).isNull();
            assertThat(cached.consecutiveRateLimits()).isZero();
        }

        @Test
        @DisplayName("[성공] remainingTokens가 maxTokens(80)으로 설정됨")
        void shouldSetDefaultMaxTokens() {
            // Given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // When
            CachedUserAgent cached = CachedUserAgent.forNew(userAgent);

            // Then
            assertThat(cached.remainingTokens()).isEqualTo(80);
            assertThat(cached.maxTokens()).isEqualTo(80);
        }
    }

    @Nested
    @DisplayName("forDbFallback() 팩토리 메서드 테스트")
    class ForDbFallback {

        @Test
        @DisplayName("[성공] IDLE 상태로 생성됨 (degraded mode)")
        void shouldCreateWithIdleStatus() {
            // Given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // When
            CachedUserAgent cached = CachedUserAgent.forDbFallback(userAgent);

            // Then
            assertThat(cached.status()).isEqualTo(UserAgentStatus.IDLE);
            assertThat(cached.sessionToken()).isNull();
        }
    }

    @Nested
    @DisplayName("forRecovery() 팩토리 메서드 테스트")
    class ForRecovery {

        @Test
        @DisplayName("[성공] SESSION_REQUIRED 상태, Health Score 70으로 생성됨")
        void shouldCreateWithSessionRequiredAndHealthScore70() {
            // When
            CachedUserAgent cached = CachedUserAgent.forRecovery(1L, "Mozilla/5.0");

            // Then
            assertThat(cached.userAgentId()).isEqualTo(1L);
            assertThat(cached.userAgentValue()).isEqualTo("Mozilla/5.0");
            assertThat(cached.status()).isEqualTo(UserAgentStatus.SESSION_REQUIRED);
            assertThat(cached.healthScore()).isEqualTo(70);
        }
    }

    @Nested
    @DisplayName("withSession() 메서드 테스트")
    class WithSession {

        @Test
        @DisplayName("[성공] 세션 토큰만 있는 경우 IDLE 상태로 전환")
        void shouldTransitionToIdleWithSessionToken() {
            // Given
            CachedUserAgent original =
                    CachedUserAgent.forNew(UserAgentFixture.anAvailableUserAgent());
            Instant expiresAt = Instant.now().plusSeconds(3600);

            // When
            CachedUserAgent updated = original.withSession("token-abc", expiresAt);

            // Then
            assertThat(updated.status()).isEqualTo(UserAgentStatus.IDLE);
            assertThat(updated.sessionToken()).isEqualTo("token-abc");
            assertThat(updated.sessionExpiresAt()).isEqualTo(expiresAt);
            assertThat(updated.userAgentId()).isEqualTo(original.userAgentId());
        }

        @Test
        @DisplayName("[성공] 세션 토큰 + 쿠키 있는 경우 IDLE 상태로 전환")
        void shouldTransitionToIdleWithSessionAndCookies() {
            // Given
            CachedUserAgent original =
                    CachedUserAgent.forNew(UserAgentFixture.anAvailableUserAgent());
            Instant expiresAt = Instant.now().plusSeconds(3600);

            // When
            CachedUserAgent updated =
                    original.withSession("token-abc", "nid-value", "uid-value", expiresAt);

            // Then
            assertThat(updated.status()).isEqualTo(UserAgentStatus.IDLE);
            assertThat(updated.sessionToken()).isEqualTo("token-abc");
            assertThat(updated.nid()).isEqualTo("nid-value");
            assertThat(updated.mustitUid()).isEqualTo("uid-value");
            assertThat(updated.consecutiveRateLimits()).isZero();
        }
    }

    @Nested
    @DisplayName("hasValidSession() 메서드 테스트")
    class HasValidSession {

        @Test
        @DisplayName("[성공] 세션 토큰이 있고 만료되지 않은 경우 true")
        void shouldReturnTrueWhenSessionValid() {
            // Given
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(3600);
            CachedUserAgent cached =
                    buildCachedUserAgent("token", null, null, expiresAt, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.hasValidSession(now)).isTrue();
        }

        @Test
        @DisplayName("[실패] 세션 토큰이 없으면 false")
        void shouldReturnFalseWhenNoToken() {
            // Given
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(3600);
            CachedUserAgent cached =
                    buildCachedUserAgent(
                            null, null, null, expiresAt, UserAgentStatus.SESSION_REQUIRED);

            // When / Then
            assertThat(cached.hasValidSession(now)).isFalse();
        }

        @Test
        @DisplayName("[실패] 세션이 만료된 경우 false")
        void shouldReturnFalseWhenSessionExpired() {
            // Given
            Instant now = Instant.now();
            Instant expiresAt = now.minusSeconds(1);
            CachedUserAgent cached =
                    buildCachedUserAgent("token", null, null, expiresAt, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.hasValidSession(now)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSessionExpired() 메서드 테스트")
    class IsSessionExpired {

        @Test
        @DisplayName("[성공] sessionExpiresAt이 현재 시간보다 이전이면 true")
        void shouldReturnTrueWhenExpired() {
            // Given
            Instant now = Instant.now();
            Instant expiresAt = now.minusSeconds(1);
            CachedUserAgent cached =
                    buildCachedUserAgent("token", null, null, expiresAt, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.isSessionExpired(now)).isTrue();
        }

        @Test
        @DisplayName("[성공] sessionExpiresAt이 null이면 false")
        void shouldReturnFalseWhenExpiresAtIsNull() {
            // Given
            Instant now = Instant.now();
            CachedUserAgent cached =
                    buildCachedUserAgent("token", null, null, null, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.isSessionExpired(now)).isFalse();
        }
    }

    @Nested
    @DisplayName("isWindowExpired() 메서드 테스트")
    class IsWindowExpired {

        @Test
        @DisplayName("[성공] windowEnd가 현재보다 이전이면 true")
        void shouldReturnTrueWhenWindowExpired() {
            // Given
            Instant now = Instant.now();
            CachedUserAgent cached =
                    new CachedUserAgent(
                            1L,
                            "ua",
                            "token",
                            null,
                            null,
                            null,
                            80,
                            80,
                            now.minusSeconds(120),
                            now.minusSeconds(60),
                            100,
                            UserAgentStatus.IDLE,
                            null,
                            null,
                            null,
                            0);

            // When / Then
            assertThat(cached.isWindowExpired(now)).isTrue();
        }

        @Test
        @DisplayName("[성공] windowEnd가 null이면 false")
        void shouldReturnFalseWhenWindowEndIsNull() {
            // Given
            Instant now = Instant.now();
            CachedUserAgent cached =
                    buildCachedUserAgent("token", null, null, null, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.isWindowExpired(now)).isFalse();
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드 테스트 (isReady, isIdle, isBorrowed, isCooldown, needsSession)")
    class StatusCheck {

        @Test
        @DisplayName("[성공] IDLE 상태에서 isReady() = true, isIdle() = true")
        void shouldReturnTrueForIdleStatus() {
            // Given
            CachedUserAgent cached =
                    buildCachedUserAgent(null, null, null, null, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.isReady()).isTrue();
            assertThat(cached.isIdle()).isTrue();
        }

        @Test
        @DisplayName("[성공] BORROWED 상태에서 isBorrowed() = true")
        void shouldReturnTrueForBorrowedStatus() {
            // Given
            CachedUserAgent cached =
                    buildCachedUserAgent(null, null, null, null, UserAgentStatus.BORROWED);

            // When / Then
            assertThat(cached.isBorrowed()).isTrue();
            assertThat(cached.isIdle()).isFalse();
        }

        @Test
        @DisplayName("[성공] COOLDOWN 상태에서 isCooldown() = true")
        void shouldReturnTrueForCooldownStatus() {
            // Given
            CachedUserAgent cached =
                    buildCachedUserAgent(null, null, null, null, UserAgentStatus.COOLDOWN);

            // When / Then
            assertThat(cached.isCooldown()).isTrue();
        }

        @Test
        @DisplayName("[성공] SESSION_REQUIRED 상태에서 needsSession() = true")
        void shouldReturnTrueForSessionRequiredStatus() {
            // Given
            CachedUserAgent cached =
                    buildCachedUserAgent(null, null, null, null, UserAgentStatus.SESSION_REQUIRED);

            // When / Then
            assertThat(cached.needsSession()).isTrue();
        }
    }

    @Nested
    @DisplayName("isRecoverable() 메서드 테스트")
    class IsRecoverable {

        @Test
        @DisplayName("[성공] SUSPENDED + suspendedAt이 threshold 이전 + healthScore >= 30이면 true")
        void shouldReturnTrueWhenRecoverable() {
            // Given
            Instant suspendedAt = Instant.now().minusSeconds(3600);
            Instant threshold = Instant.now().minusSeconds(1800);
            CachedUserAgent cached =
                    new CachedUserAgent(
                            1L,
                            "ua",
                            null,
                            null,
                            null,
                            null,
                            80,
                            80,
                            null,
                            null,
                            50,
                            UserAgentStatus.SUSPENDED,
                            suspendedAt,
                            null,
                            null,
                            0);

            // When / Then
            assertThat(cached.isRecoverable(threshold)).isTrue();
        }

        @Test
        @DisplayName("[실패] healthScore < 30이면 false")
        void shouldReturnFalseWhenHealthScoreTooLow() {
            // Given
            Instant suspendedAt = Instant.now().minusSeconds(3600);
            Instant threshold = Instant.now().minusSeconds(1800);
            CachedUserAgent cached =
                    new CachedUserAgent(
                            1L,
                            "ua",
                            null,
                            null,
                            null,
                            null,
                            80,
                            80,
                            null,
                            null,
                            20,
                            UserAgentStatus.SUSPENDED,
                            suspendedAt,
                            null,
                            null,
                            0);

            // When / Then
            assertThat(cached.isRecoverable(threshold)).isFalse();
        }

        @Test
        @DisplayName("[실패] SUSPENDED가 아닌 상태이면 false")
        void shouldReturnFalseWhenNotSuspended() {
            // Given
            Instant suspendedAt = Instant.now().minusSeconds(3600);
            Instant threshold = Instant.now().minusSeconds(1800);
            CachedUserAgent cached =
                    new CachedUserAgent(
                            1L,
                            "ua",
                            null,
                            null,
                            null,
                            null,
                            80,
                            80,
                            null,
                            null,
                            80,
                            UserAgentStatus.IDLE,
                            suspendedAt,
                            null,
                            null,
                            0);

            // When / Then
            assertThat(cached.isRecoverable(threshold)).isFalse();
        }

        @Test
        @DisplayName("[실패] suspendedAt이 threshold 이후이면 false")
        void shouldReturnFalseWhenSuspendedAfterThreshold() {
            // Given
            Instant threshold = Instant.now().minusSeconds(3600);
            Instant suspendedAt = Instant.now().minusSeconds(1800);
            CachedUserAgent cached =
                    new CachedUserAgent(
                            1L,
                            "ua",
                            null,
                            null,
                            null,
                            null,
                            80,
                            80,
                            null,
                            null,
                            80,
                            UserAgentStatus.SUSPENDED,
                            suspendedAt,
                            null,
                            null,
                            0);

            // When / Then
            assertThat(cached.isRecoverable(threshold)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasSearchCookies() 메서드 테스트")
    class HasSearchCookies {

        @Test
        @DisplayName("[성공] nid와 mustitUid 모두 있으면 true")
        void shouldReturnTrueWhenBothCookiesPresent() {
            // Given
            CachedUserAgent cached =
                    buildCachedUserAgent("token", "nid-val", "uid-val", null, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.hasSearchCookies()).isTrue();
        }

        @Test
        @DisplayName("[실패] nid가 없으면 false")
        void shouldReturnFalseWhenNidMissing() {
            // Given
            CachedUserAgent cached =
                    buildCachedUserAgent("token", null, "uid-val", null, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("[실패] mustitUid가 없으면 false")
        void shouldReturnFalseWhenMustitUidMissing() {
            // Given
            CachedUserAgent cached =
                    buildCachedUserAgent("token", "nid-val", null, null, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("[실패] nid가 빈 문자열이면 false")
        void shouldReturnFalseWhenNidBlank() {
            // Given
            CachedUserAgent cached =
                    buildCachedUserAgent("token", "  ", "uid-val", null, UserAgentStatus.IDLE);

            // When / Then
            assertThat(cached.hasSearchCookies()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasTokens() 메서드 테스트")
    class HasTokens {

        @Test
        @DisplayName("[성공] remainingTokens > 0이면 true")
        void shouldReturnTrueWhenHasTokens() {
            // Given
            CachedUserAgent cached =
                    new CachedUserAgent(
                            1L,
                            "ua",
                            null,
                            null,
                            null,
                            null,
                            5,
                            80,
                            null,
                            null,
                            100,
                            UserAgentStatus.IDLE,
                            null,
                            null,
                            null,
                            0);

            // When / Then
            assertThat(cached.hasTokens()).isTrue();
        }

        @Test
        @DisplayName("[실패] remainingTokens = 0이면 false")
        void shouldReturnFalseWhenNoTokens() {
            // Given
            CachedUserAgent cached =
                    new CachedUserAgent(
                            1L,
                            "ua",
                            null,
                            null,
                            null,
                            null,
                            0,
                            80,
                            null,
                            null,
                            100,
                            UserAgentStatus.IDLE,
                            null,
                            null,
                            null,
                            0);

            // When / Then
            assertThat(cached.hasTokens()).isFalse();
        }
    }

    // Helper factory
    private CachedUserAgent buildCachedUserAgent(
            String sessionToken,
            String nid,
            String mustitUid,
            Instant sessionExpiresAt,
            UserAgentStatus status) {
        return new CachedUserAgent(
                1L,
                "Mozilla/5.0",
                sessionToken,
                nid,
                mustitUid,
                sessionExpiresAt,
                80,
                80,
                null,
                null,
                100,
                status,
                null,
                null,
                null,
                0);
    }
}
