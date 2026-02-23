package com.ryuqq.crawlinghub.domain.useragent.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentStateException;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@DisplayName("UserAgent Borrow/Return 비즈니스 로직 단위 테스트")
class UserAgentBorrowReturnTest {

    private static final Instant FIXED_INSTANT = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("markBorrowed() 테스트")
    class MarkBorrowedTest {

        @Test
        @DisplayName("IDLE 상태에서 BORROWED로 전환한다")
        void idleTransitionsToBorrowed() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            userAgent.markBorrowed(FIXED_INSTANT);

            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.BORROWED);
            assertThat(userAgent.getRequestsPerDay()).isEqualTo(1);
            assertThat(userAgent.getLastUsedAt()).isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("IDLE이 아닌 상태에서 markBorrowed() 호출 시 예외가 발생한다")
        void nonIdleThrowsException() {
            UserAgent userAgent = UserAgentFixture.aSuspendedUserAgent();

            assertThatThrownBy(() -> userAgent.markBorrowed(FIXED_INSTANT))
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }
    }

    @Nested
    @DisplayName("returnSuccess() 테스트")
    class ReturnSuccessTest {

        @Test
        @DisplayName("BORROWED 상태에서 IDLE로 전환하고 Health Score가 증가한다")
        void borrowedTransitionsToIdleWithHealthIncrease() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            userAgent.markBorrowed(FIXED_INSTANT);
            int scoreBeforeReturn = userAgent.getHealthScoreValue();

            userAgent.returnSuccess(FIXED_INSTANT);

            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.IDLE);
            assertThat(userAgent.getHealthScoreValue()).isGreaterThanOrEqualTo(scoreBeforeReturn);
        }

        @Test
        @DisplayName("BORROWED 상태가 아닐 때 returnSuccess() 호출 시 예외가 발생한다")
        void nonBorrowedThrowsException() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            assertThatThrownBy(() -> userAgent.returnSuccess(FIXED_INSTANT))
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }
    }

    @Nested
    @DisplayName("returnWithCooldown() 테스트")
    class ReturnWithCooldownTest {

        @Test
        @DisplayName("429 응답 시 COOLDOWN 상태로 전환한다")
        void rateLimitResponseTransitionsToCooldown() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            userAgent.markBorrowed(FIXED_INSTANT);

            userAgent.returnWithCooldown(429, FIXED_INSTANT);

            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.COOLDOWN);
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(80);
        }

        @Test
        @DisplayName("5xx 응답이고 Health Score가 임계값 이하이면 SUSPENDED로 전환한다")
        void serverErrorBelowThresholdTransitionsToSuspended() {
            UserAgent userAgent = UserAgentFixture.anAlmostSuspendedUserAgent();
            userAgent.markBorrowed(FIXED_INSTANT);

            userAgent.returnWithCooldown(500, FIXED_INSTANT);

            assertThat(userAgent.isSuspended()).isTrue();
        }

        @Test
        @DisplayName("5xx 응답이고 Health Score가 임계값 이상이면 IDLE로 전환한다")
        void serverErrorAboveThresholdTransitionsToIdle() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            userAgent.markBorrowed(FIXED_INSTANT);

            userAgent.returnWithCooldown(500, FIXED_INSTANT);

            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.IDLE);
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(90);
        }

        @Test
        @DisplayName("BORROWED 상태가 아닐 때 returnWithCooldown() 호출 시 예외가 발생한다")
        void nonBorrowedThrowsException() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            assertThatThrownBy(() -> userAgent.returnWithCooldown(429, FIXED_INSTANT))
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }
    }

    @Nested
    @DisplayName("recoverFromCooldown() 테스트")
    class RecoverFromCooldownTest {

        private UserAgent aCooldownUserAgent() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            userAgent.markBorrowed(FIXED_INSTANT);
            userAgent.returnWithCooldown(429, FIXED_INSTANT);
            return userAgent;
        }

        @Test
        @DisplayName("세션이 유효하면 COOLDOWN에서 IDLE로 전환한다")
        void validSessionTransitionsToIdle() {
            UserAgent userAgent = aCooldownUserAgent();

            userAgent.recoverFromCooldown(FIXED_INSTANT, true);

            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.IDLE);
        }

        @Test
        @DisplayName("세션이 없으면 COOLDOWN에서 SESSION_REQUIRED로 전환한다")
        void noSessionTransitionsToSessionRequired() {
            UserAgent userAgent = aCooldownUserAgent();

            userAgent.recoverFromCooldown(FIXED_INSTANT, false);

            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.SESSION_REQUIRED);
        }

        @Test
        @DisplayName("COOLDOWN 상태가 아닐 때 호출 시 예외가 발생한다")
        void nonCooldownThrowsException() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            assertThatThrownBy(() -> userAgent.recoverFromCooldown(FIXED_INSTANT, true))
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }
    }

    @Nested
    @DisplayName("unblock() 테스트")
    class UnblockTest {

        @Test
        @DisplayName("BLOCKED 상태에서 IDLE로 전환하고 Health Score가 70이 된다")
        void blockedTransitionsToIdle() {
            UserAgent userAgent = UserAgentFixture.aBlockedUserAgent();

            userAgent.unblock(FIXED_INSTANT);

            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.IDLE);
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(70);
        }

        @Test
        @DisplayName("BLOCKED 상태가 아닐 때 unblock() 호출 시 예외가 발생한다")
        void nonBlockedThrowsException() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            assertThatThrownBy(() -> userAgent.unblock(FIXED_INSTANT))
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }
    }

    @Nested
    @DisplayName("changeStatus() 테스트")
    class ChangeStatusTest {

        @Test
        @DisplayName("다른 상태로 변경한다")
        void changeToNewStatus() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            userAgent.changeStatus(UserAgentStatus.SUSPENDED, FIXED_INSTANT);

            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.SUSPENDED);
        }

        @Test
        @DisplayName("IDLE로 변경 시 Health Score가 70이 된다")
        void changeToIdleResetsHealthScore() {
            UserAgent userAgent = UserAgentFixture.aSuspendedUserAgent();

            userAgent.changeStatus(UserAgentStatus.IDLE, FIXED_INSTANT);

            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.IDLE);
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(70);
        }

        @Test
        @DisplayName("같은 상태로 변경 시 예외가 발생한다")
        void sameStatusThrowsException() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            assertThatThrownBy(() -> userAgent.changeStatus(UserAgentStatus.IDLE, FIXED_INSTANT))
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }
    }

    @Nested
    @DisplayName("resetHealth() 테스트")
    class ResetHealthTest {

        @Test
        @DisplayName("Health Score가 100으로 초기화된다")
        void resetHealthToFull() {
            UserAgent userAgent = UserAgentFixture.anAlmostSuspendedUserAgent();
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(35);

            userAgent.resetHealth(FIXED_INSTANT);

            assertThat(userAgent.getHealthScoreValue()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("429 연속 5회 에스컬레이션 테스트")
    class CooldownEscalationTest {

        @Test
        @DisplayName("429 연속 5회 시 SUSPENDED로 에스컬레이션한다")
        void fiveConsecutiveRateLimitsEscalatesToSuspended() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            for (int i = 0; i < 5; i++) {
                if (userAgent.getStatus() == UserAgentStatus.IDLE) {
                    userAgent.markBorrowed(FIXED_INSTANT);
                }
                if (userAgent.getStatus() == UserAgentStatus.BORROWED) {
                    userAgent.returnWithCooldown(429, FIXED_INSTANT);
                }
                if (userAgent.getStatus() == UserAgentStatus.COOLDOWN) {
                    userAgent.recoverFromCooldown(FIXED_INSTANT, true);
                    if (i < 4) {
                        userAgent.markBorrowed(FIXED_INSTANT);
                    }
                }
            }

            assertThat(userAgent.isSuspended()).isTrue();
        }
    }
}
