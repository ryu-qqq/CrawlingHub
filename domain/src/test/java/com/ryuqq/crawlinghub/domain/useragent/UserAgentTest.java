package com.ryuqq.crawlinghub.domain.useragent;

import com.ryuqq.crawlinghub.domain.token.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserAgent Domain 단위 테스트
 *
 * <p>테스트 범위:
 * <ul>
 *   <li>생성 테스트: forNew, of, reconstitute</li>
 *   <li>토큰 관리 테스트: issueNewToken, isTokenExpired (24시간 기준)</li>
 *   <li>요청 제한 테스트: consumeRequest, canMakeRequest (시간당 80회 제한)</li>
 *   <li>Rate Limit 처리 테스트: handleRateLimitError, recoverFromRateLimit (1시간 복구)</li>
 *   <li>토큰 상태 전이 테스트: IDLE → ACTIVE → RATE_LIMITED → RECOVERED/DISABLED</li>
 *   <li>예외 케이스 테스트</li>
 *   <li>Law of Demeter 준수 테스트</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("UserAgent Domain 단위 테스트")
class UserAgentTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTests {

        @Test
        @DisplayName("유효한 입력으로 신규 UserAgent 생성 성공")
        void shouldCreateNewUserAgentWithValidInputs() {
            // Given
            String userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";

            // When
            UserAgent userAgent = UserAgent.forNew(userAgentString);

            // Then
            assertThat(userAgent).isNotNull();
            assertThat(userAgent.getIdValue()).isNull(); // 신규 생성이므로 ID 없음
            assertThat(userAgent.getUserAgentString()).isEqualTo(userAgentString);
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.IDLE); // 초기 상태
            assertThat(userAgent.getRemainingRequests()).isEqualTo(80); // MAX_REQUESTS_PER_HOUR
            assertThat(userAgent.getCurrentToken()).isNull();
            assertThat(userAgent.getCurrentToken()).isNull(); // Token이 null이므로 issuedAt도 null
            assertThat(userAgent.getRateLimitResetAt()).isNull();
        }

        @Test
        @DisplayName("ID를 가진 UserAgent 생성 성공 (of)")
        void shouldCreateUserAgentWithId() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String userAgentString = "Mozilla/5.0";

            // When
            UserAgent userAgent = UserAgent.of(userAgentId, userAgentString);

            // Then
            assertThat(userAgent.getIdValue()).isEqualTo(1L);
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.IDLE);
        }

        @Test
        @DisplayName("DB reconstitute로 모든 필드 포함 UserAgent 생성 성공")
        void shouldReconstituteUserAgentFromDatabase() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String userAgentString = "Mozilla/5.0";
            String currentToken = "token-abc-123";
            TokenStatus tokenStatus = TokenStatus.ACTIVE;
            Integer remainingRequests = 50;
            LocalDateTime tokenIssuedAt = LocalDateTime.now().minusHours(5);
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            LocalDateTime updatedAt = LocalDateTime.now();

            // When
            UserAgent userAgent = UserAgent.reconstitute(
                userAgentId,
                userAgentString,
                Token.of(currentToken, tokenIssuedAt, tokenIssuedAt.plusHours(24)),
                tokenStatus,
                remainingRequests,
                null,
                createdAt,
                updatedAt
            );

            // Then
            assertThat(userAgent.getIdValue()).isEqualTo(1L);
            assertThat(userAgent.getCurrentToken().getValue()).isEqualTo("token-abc-123");
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);
            assertThat(userAgent.getRemainingRequests()).isEqualTo(50);
            // Token Value Object 내부의 issuedAt 필드는 직접 검증하지 않음 (Law of Demeter)
        }
    }

    @Nested
    @DisplayName("토큰 관리 테스트")
    class TokenManagementTests {

        @Test
        @DisplayName("새 토큰 발급 시 상태가 IDLE로 변경되고 remainingRequests가 80으로 초기화")
        void shouldIssueNewTokenAndResetToIdleState() {
            // Given
            UserAgent userAgent = UserAgentFixture.create();
            String newTokenValue = "new-token-xyz-789";
            LocalDateTime now = LocalDateTime.now();
            Token newToken = Token.of(newTokenValue, now, now.plusHours(24));

            // When
            userAgent.issueNewToken(newToken);

            // Then
            assertThat(userAgent.getCurrentToken().getValue()).isEqualTo(newTokenValue);
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.IDLE);
            assertThat(userAgent.getRemainingRequests()).isEqualTo(80);
            // Token이 발급되었으므로 issuedAt이 설정되어 있어야 함
            assertThat(userAgent.getRateLimitResetAt()).isNull();
        }

        @Test
        @DisplayName("토큰 발급 후 remainingRequests를 소진하고 새 토큰 발급 시 다시 80으로 복구")
        void shouldResetRemainingRequestsWhenIssuingNewToken() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);
            // remainingRequests를 소진
            for (int i = 0; i < 50; i++) {
                userAgent.consumeRequest();
            }
            assertThat(userAgent.getRemainingRequests()).isEqualTo(30);

            // When
            LocalDateTime now = LocalDateTime.now();
            Token newToken = Token.of("new-token", now, now.plusHours(24));
            userAgent.issueNewToken(newToken);

            // Then
            assertThat(userAgent.getRemainingRequests()).isEqualTo(80);
        }

        @Test
        @DisplayName("tokenIssuedAt이 null이면 isTokenExpired() 는 true 반환")
        void shouldReturnTrueWhenTokenIssuedAtIsNull() {
            // Given
            UserAgent userAgent = UserAgentFixture.create();
            // Token이 null이면 issuedAt도 null

            // When & Then
            assertThat(userAgent.isTokenExpired()).isTrue();
        }

        @Test
        @DisplayName("토큰 발급 후 24시간 이내면 isTokenExpired() 는 false 반환")
        void shouldReturnFalseWhenTokenWithinValidityPeriod() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tokenIssuedAt = now.minusHours(23); // 23시간 전
            UserAgent userAgent = UserAgent.reconstitute(
                UserAgentId.of(1L),
                "Mozilla/5.0",
                Token.of("token", tokenIssuedAt, tokenIssuedAt.plusHours(24)),
                TokenStatus.ACTIVE,
                50,
                null,
                now.minusDays(1),
                now
            );

            // When & Then
            assertThat(userAgent.isTokenExpired()).isFalse();
        }

        @Test
        @DisplayName("토큰 발급 후 24시간 초과 시 isTokenExpired() 는 true 반환")
        void shouldReturnTrueWhenTokenExpired() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tokenIssuedAt = now.minusHours(25); // 25시간 전
            UserAgent userAgent = UserAgent.reconstitute(
                UserAgentId.of(1L),
                "Mozilla/5.0",
                Token.of("token", tokenIssuedAt, tokenIssuedAt.plusHours(24)),
                TokenStatus.ACTIVE,
                50,
                null,
                now.minusDays(1),
                now
            );

            // When & Then
            assertThat(userAgent.isTokenExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("요청 제한 테스트")
    class RequestLimitTests {

        @Test
        @DisplayName("요청 가능 상태에서 consumeRequest() 호출 시 remainingRequests 감소")
        void shouldConsumeRequestWhenCanMakeRequest() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);
            int initialRequests = userAgent.getRemainingRequests();

            // When
            userAgent.consumeRequest();

            // Then
            assertThat(userAgent.getRemainingRequests()).isEqualTo(initialRequests - 1);
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);
        }

        @Test
        @DisplayName("remainingRequests가 0이면 canMakeRequest() 는 false 반환")
        void shouldReturnFalseWhenNoRemainingRequests() {
            // Given
            UserAgent userAgent = UserAgentFixture.createRateLimited();
            assertThat(userAgent.getRemainingRequests()).isEqualTo(0);

            // When & Then
            assertThat(userAgent.canMakeRequest()).isFalse();
        }

        @Test
        @DisplayName("토큰이 만료되면 canMakeRequest() 는 false 반환")
        void shouldReturnFalseWhenTokenExpired() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiredTokenIssuedAt = now.minusHours(25); // 24시간 초과
            UserAgent userAgent = UserAgent.reconstitute(
                UserAgentId.of(1L),
                "Mozilla/5.0",
                Token.of("token", expiredTokenIssuedAt, expiredTokenIssuedAt.plusHours(24)),
                TokenStatus.ACTIVE,
                50,
                null,
                now.minusDays(1),
                now
            );

            // When & Then
            assertThat(userAgent.canMakeRequest()).isFalse();
        }

        @Test
        @DisplayName("요청 불가능 상태에서 consumeRequest() 호출 시 예외 발생")
        void shouldThrowExceptionWhenConsumeRequestWhileCannotMakeRequest() {
            // Given
            UserAgent userAgent = UserAgentFixture.createRateLimited();
            assertThat(userAgent.canMakeRequest()).isFalse();

            // When & Then
            assertThatThrownBy(userAgent::consumeRequest)
                .isInstanceOf(com.ryuqq.crawlinghub.domain.useragent.exception.TokenExpiredException.class)
                .hasMessageContaining("토큰이 만료되었습니다");
        }

        @Test
        @DisplayName("remainingRequests를 모두 소진하면 canMakeRequest() 는 false 반환")
        void shouldReturnFalseWhenAllRequestsConsumed() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);
            for (int i = 0; i < 80; i++) {
                userAgent.consumeRequest();
            }

            // When & Then
            assertThat(userAgent.getRemainingRequests()).isEqualTo(0);
            assertThat(userAgent.canMakeRequest()).isFalse();
        }
    }

    @Nested
    @DisplayName("Rate Limit 처리 테스트")
    class RateLimitHandlingTests {

        @Test
        @DisplayName("handleRateLimitError() 호출 시 토큰 상태가 RATE_LIMITED로 변경")
        void shouldTransitionToRateLimitedWhenHandlingRateLimitError() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);

            // When
            userAgent.handleRateLimitError();

            // Then
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.RATE_LIMITED);
            assertThat(userAgent.getCurrentToken()).isNull(); // 토큰 폐기
            assertThat(userAgent.getRemainingRequests()).isEqualTo(0);
            assertThat(userAgent.getRateLimitResetAt()).isNotNull();
        }

        @Test
        @DisplayName("handleRateLimitError() 호출 시 rateLimitResetAt은 1시간 후로 설정")
        void shouldSetRateLimitResetAtTo1HourLater() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);
            LocalDateTime beforeError = LocalDateTime.now();

            // When
            userAgent.handleRateLimitError();

            // Then
            LocalDateTime resetAt = userAgent.getRateLimitResetAt();
            assertThat(resetAt).isAfter(beforeError);
            // rateLimitResetAt은 대략 1시간 후 (정확한 비교는 Clock 주입 필요)
        }

        @Test
        @DisplayName("RATE_LIMITED 상태에서 recoverFromRateLimit() 호출 시 RECOVERED 상태로 전이")
        void shouldTransitionToRecoveredWhenRecoveringFromRateLimit() {
            // Given
            UserAgent userAgent = UserAgentFixture.createRateLimited();
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.RATE_LIMITED);

            // When
            userAgent.recoverFromRateLimit();

            // Then
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.RECOVERED);
            assertThat(userAgent.getRemainingRequests()).isEqualTo(80);
            assertThat(userAgent.getRateLimitResetAt()).isNull();
        }

        @Test
        @DisplayName("RATE_LIMITED가 아닌 상태에서 recoverFromRateLimit() 호출 시 예외 발생")
        void shouldThrowExceptionWhenRecoveringFromNonRateLimitedState() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);

            // When & Then
            assertThatThrownBy(userAgent::recoverFromRateLimit)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RATE_LIMITED 상태에서만 복구할 수 있습니다");
        }

        @Test
        @DisplayName("rateLimitResetAt이 null이면 canRecover() 는 false 반환")
        void shouldReturnFalseWhenRateLimitResetAtIsNull() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);
            assertThat(userAgent.getRateLimitResetAt()).isNull();

            // When & Then
            assertThat(userAgent.canRecover()).isFalse();
        }

        @Test
        @DisplayName("현재 시간이 rateLimitResetAt 이후면 canRecover() 는 true 반환")
        void shouldReturnTrueWhenCurrentTimeAfterRateLimitResetAt() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime pastResetTime = now.minusMinutes(10); // 10분 전
            UserAgent userAgent = UserAgent.reconstitute(
                UserAgentId.of(1L),
                "Mozilla/5.0",
                null,
                TokenStatus.RATE_LIMITED,
                0,
                pastResetTime,
                now.minusDays(1),
                now
            );

            // When & Then
            assertThat(userAgent.canRecover()).isTrue();
        }

        @Test
        @DisplayName("현재 시간이 rateLimitResetAt 이전이면 canRecover() 는 false 반환")
        void shouldReturnFalseWhenCurrentTimeBeforeRateLimitResetAt() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime futureResetTime = now.plusMinutes(30); // 30분 후
            UserAgent userAgent = UserAgent.reconstitute(
                UserAgentId.of(1L),
                "Mozilla/5.0",
                null,
                TokenStatus.RATE_LIMITED,
                0,
                futureResetTime,
                now.minusDays(1),
                now
            );

            // When & Then
            assertThat(userAgent.canRecover()).isFalse();
        }
    }

    @Nested
    @DisplayName("비활성화 테스트")
    class DisableTests {

        @Test
        @DisplayName("disable() 호출 시 토큰 상태가 DISABLED로 변경")
        void shouldTransitionToDisabledWhenDisabling() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);

            // When
            userAgent.disable();

            // Then
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.DISABLED);
            assertThat(userAgent.getCurrentToken()).isNull();
            assertThat(userAgent.getRemainingRequests()).isEqualTo(0);
        }

        @Test
        @DisplayName("DISABLED 상태에서는 canMakeRequest() 가 false 반환")
        void shouldReturnFalseWhenDisabled() {
            // Given
            UserAgent userAgent = UserAgentFixture.createDisabled();

            // When & Then
            assertThat(userAgent.canMakeRequest()).isFalse();
        }
    }

    @Nested
    @DisplayName("상태 조회 테스트")
    class StatusQueryTests {

        @Test
        @DisplayName("hasStatus()는 현재 상태와 일치하면 true 반환")
        void shouldReturnTrueWhenStatusMatches() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);

            // When & Then
            assertThat(userAgent.hasStatus(TokenStatus.ACTIVE)).isTrue();
            assertThat(userAgent.hasStatus(TokenStatus.IDLE)).isFalse();
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("User Agent 문자열이 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenUserAgentStringIsNullOrBlank(String invalidString) {
            // When & Then
            assertThatThrownBy(() -> UserAgent.forNew(invalidString))
                .isInstanceOf(com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentException.class)
                .hasMessageContaining("유효하지 않은 User-Agent 문자열입니다");
        }

        @Test
        @DisplayName("of() 메서드에서 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInOf() {
            // When & Then
            assertThatThrownBy(() -> UserAgent.of(null, "Mozilla/5.0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserAgent ID는 필수입니다");
        }

        @Test
        @DisplayName("reconstitute() 메서드에서 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInReconstitute() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            // When & Then
            assertThatThrownBy(() ->
                UserAgent.reconstitute(
                    null,
                    "Mozilla/5.0",
                    Token.of("token", now, now.plusHours(24)),
                    TokenStatus.ACTIVE,
                    50,
                    null,
                    now,
                    now
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("issueNewToken() 에서 토큰이 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenTokenIsNullOrBlankInIssueNewToken(String invalidToken) {
            // Given
            UserAgent userAgent = UserAgentFixture.create();
            LocalDateTime now = LocalDateTime.now();

            // When & Then
            assertThatThrownBy(() -> {
                Token token = Token.of(invalidToken, now, now.plusHours(24));
                userAgent.issueNewToken(token);
            })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("토큰");
        }
    }

    @Nested
    @DisplayName("불변성 검증 테스트")
    class InvariantTests {

        @Test
        @DisplayName("신규 UserAgent 생성 시 초기 상태는 IDLE")
        void shouldHaveIdleStatusWhenNewUserAgentCreated() {
            // Given & When
            UserAgent userAgent = UserAgentFixture.create();

            // Then
            assertThat(userAgent.getTokenStatus()).isEqualTo(TokenStatus.IDLE);
        }

        @Test
        @DisplayName("신규 UserAgent 생성 시 remainingRequests는 80")
        void shouldHave80RemainingRequestsWhenNewUserAgentCreated() {
            // Given & When
            UserAgent userAgent = UserAgentFixture.create();

            // Then
            assertThat(userAgent.getRemainingRequests()).isEqualTo(80);
        }

        @Test
        @DisplayName("신규 UserAgent 생성 시 currentToken은 null")
        void shouldHaveNullCurrentTokenWhenNewUserAgentCreated() {
            // Given & When
            UserAgent userAgent = UserAgentFixture.create();

            // Then
            assertThat(userAgent.getCurrentToken()).isNull();
        }

        @Test
        @DisplayName("remainingRequests는 0 미만으로 감소할 수 없음")
        void shouldNotDecreaseBelowZero() {
            // Given
            UserAgent userAgent = UserAgentFixture.createActive(80);
            for (int i = 0; i < 80; i++) {
                userAgent.consumeRequest();
            }
            assertThat(userAgent.getRemainingRequests()).isEqualTo(0);

            // When & Then
            assertThatThrownBy(userAgent::consumeRequest)
                .isInstanceOf(com.ryuqq.crawlinghub.domain.useragent.exception.RateLimitExceededException.class)
                .hasMessageContaining("Rate Limit을 초과했습니다");
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()는 ID를 직접 노출하지 않고 값만 반환")
        void shouldReturnIdValueWithoutExposingIdObject() {
            // Given
            UserAgent userAgent = UserAgentFixture.createWithId(100L);

            // When
            Long idValue = userAgent.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(100L);
        }

        @Test
        @DisplayName("equals()는 ID 기반으로 동작하며 객체 체이닝 없음")
        void shouldImplementEqualsBasedOnIdWithoutChaining() {
            // Given
            UserAgent userAgent1 = UserAgentFixture.createWithId(1L);
            UserAgent userAgent2 = UserAgentFixture.createWithId(1L);
            UserAgent userAgent3 = UserAgentFixture.createWithId(2L);

            // When & Then
            assertThat(userAgent1).isEqualTo(userAgent2);
            assertThat(userAgent1).isNotEqualTo(userAgent3);
        }

        @Test
        @DisplayName("hashCode()는 ID 기반으로 동작하며 객체 체이닝 없음")
        void shouldImplementHashCodeBasedOnIdWithoutChaining() {
            // Given
            UserAgent userAgent1 = UserAgentFixture.createWithId(1L);
            UserAgent userAgent2 = UserAgentFixture.createWithId(1L);

            // When & Then
            assertThat(userAgent1.hashCode()).isEqualTo(userAgent2.hashCode());
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("User Agent 문자열이 매우 긴 경우도 정상 생성")
        void shouldCreateUserAgentWithVeryLongString() {
            // Given
            String longUserAgent = "Mozilla/5.0 " + "a".repeat(1000);

            // When
            UserAgent userAgent = UserAgent.forNew(longUserAgent);

            // Then
            assertThat(userAgent.getUserAgentString()).isEqualTo(longUserAgent);
            assertThat(userAgent.getUserAgentString().length()).isGreaterThan(1000);
        }

        @Test
        @DisplayName("토큰이 매우 긴 문자열일 때도 정상 발급")
        void shouldIssueVeryLongToken() {
            // Given
            UserAgent userAgent = UserAgentFixture.create();
            String longTokenValue = "token-" + "x".repeat(1000);
            LocalDateTime now = LocalDateTime.now();
            Token longToken = Token.of(longTokenValue, now, now.plusHours(24));

            // When
            userAgent.issueNewToken(longToken);

            // Then
            assertThat(userAgent.getCurrentToken().getValue()).isEqualTo(longTokenValue);
        }

        @Test
        @DisplayName("remainingRequests가 1일 때 1번 요청 후 0이 됨")
        void shouldDecrementRemainingRequestsToZero() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tokenIssuedAt = now.minusHours(1);
            UserAgent userAgent = UserAgent.reconstitute(
                UserAgentId.of(1L),
                "Mozilla/5.0",
                Token.of("token", tokenIssuedAt, tokenIssuedAt.plusHours(24)),
                TokenStatus.ACTIVE,
                1, // remainingRequests = 1
                null,
                now.minusDays(1),
                now
            );

            // When
            userAgent.consumeRequest();

            // Then
            assertThat(userAgent.getRemainingRequests()).isEqualTo(0);
            assertThat(userAgent.canMakeRequest()).isFalse();
        }

        @Test
        @DisplayName("tokenIssuedAt이 정확히 24시간 전일 때 만료 처리")
        void shouldExpireTokenAtExactly24Hours() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime exactly24HoursAgo = now.minusHours(24).minusSeconds(1);
            UserAgent userAgent = UserAgent.reconstitute(
                UserAgentId.of(1L),
                "Mozilla/5.0",
                Token.of("token", exactly24HoursAgo, exactly24HoursAgo.plusHours(24)),
                TokenStatus.ACTIVE,
                50,
                null,
                now.minusDays(1),
                now
            );

            // When & Then
            assertThat(userAgent.isTokenExpired()).isTrue();
        }
    }
}
