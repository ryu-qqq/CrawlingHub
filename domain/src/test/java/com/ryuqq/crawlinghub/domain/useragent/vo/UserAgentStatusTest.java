package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("UserAgentStatus 단위 테스트")
class UserAgentStatusTest {

    @Nested
    @DisplayName("isIdle() 테스트")
    class IsIdleTest {

        @Test
        @DisplayName("IDLE이면 true를 반환한다")
        void idleReturnsTrue() {
            assertThat(UserAgentStatus.IDLE.isIdle()).isTrue();
        }

        @Test
        @DisplayName("IDLE이 아니면 false를 반환한다")
        void nonIdleReturnsFalse() {
            assertThat(UserAgentStatus.BORROWED.isIdle()).isFalse();
            assertThat(UserAgentStatus.SUSPENDED.isIdle()).isFalse();
            assertThat(UserAgentStatus.BLOCKED.isIdle()).isFalse();
        }
    }

    @Nested
    @DisplayName("isBorrowed() 테스트")
    class IsBorrowedTest {

        @Test
        @DisplayName("BORROWED이면 true를 반환한다")
        void borrowedReturnsTrue() {
            assertThat(UserAgentStatus.BORROWED.isBorrowed()).isTrue();
        }

        @Test
        @DisplayName("BORROWED가 아니면 false를 반환한다")
        void nonBorrowedReturnsFalse() {
            assertThat(UserAgentStatus.IDLE.isBorrowed()).isFalse();
            assertThat(UserAgentStatus.COOLDOWN.isBorrowed()).isFalse();
        }
    }

    @Nested
    @DisplayName("isCooldown() 테스트")
    class IsCooldownTest {

        @Test
        @DisplayName("COOLDOWN이면 true를 반환한다")
        void cooldownReturnsTrue() {
            assertThat(UserAgentStatus.COOLDOWN.isCooldown()).isTrue();
        }

        @Test
        @DisplayName("COOLDOWN이 아니면 false를 반환한다")
        void nonCooldownReturnsFalse() {
            assertThat(UserAgentStatus.IDLE.isCooldown()).isFalse();
            assertThat(UserAgentStatus.SUSPENDED.isCooldown()).isFalse();
        }
    }

    @Nested
    @DisplayName("needsSession() 테스트")
    class NeedsSessionTest {

        @Test
        @DisplayName("SESSION_REQUIRED이면 true를 반환한다")
        void sessionRequiredReturnsTrue() {
            assertThat(UserAgentStatus.SESSION_REQUIRED.needsSession()).isTrue();
        }

        @Test
        @DisplayName("SESSION_REQUIRED가 아니면 false를 반환한다")
        void nonSessionRequiredReturnsFalse() {
            assertThat(UserAgentStatus.IDLE.needsSession()).isFalse();
            assertThat(UserAgentStatus.SUSPENDED.needsSession()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSuspended() 테스트")
    class IsSuspendedTest {

        @Test
        @DisplayName("SUSPENDED이면 true를 반환한다")
        void suspendedReturnsTrue() {
            assertThat(UserAgentStatus.SUSPENDED.isSuspended()).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED가 아니면 false를 반환한다")
        void nonSuspendedReturnsFalse() {
            assertThat(UserAgentStatus.IDLE.isSuspended()).isFalse();
            assertThat(UserAgentStatus.BLOCKED.isSuspended()).isFalse();
        }
    }

    @Nested
    @DisplayName("canBorrow() 테스트")
    class CanBorrowTest {

        @Test
        @DisplayName("IDLE이면 borrow 가능하다")
        void idleCanBorrow() {
            assertThat(UserAgentStatus.IDLE.canBorrow()).isTrue();
        }

        @Test
        @DisplayName("IDLE이 아니면 borrow 불가능하다")
        void nonIdleCannotBorrow() {
            assertThat(UserAgentStatus.BORROWED.canBorrow()).isFalse();
            assertThat(UserAgentStatus.SUSPENDED.canBorrow()).isFalse();
            assertThat(UserAgentStatus.BLOCKED.canBorrow()).isFalse();
        }
    }

    @Nested
    @DisplayName("canRecover() 테스트")
    class CanRecoverTest {

        @Test
        @DisplayName("SUSPENDED이면 복구 가능하다")
        void suspendedCanRecover() {
            assertThat(UserAgentStatus.SUSPENDED.canRecover()).isTrue();
        }

        @Test
        @DisplayName("BLOCKED이면 복구 불가능하다")
        void blockedCannotRecover() {
            assertThat(UserAgentStatus.BLOCKED.canRecover()).isFalse();
        }

        @Test
        @DisplayName("IDLE이면 복구 불가능하다")
        void idleCannotRecover() {
            assertThat(UserAgentStatus.IDLE.canRecover()).isFalse();
        }
    }

    @Nested
    @DisplayName("canAutoRecover() 테스트")
    class CanAutoRecoverTest {

        @Test
        @DisplayName("COOLDOWN이면 자동 복구 가능하다")
        void cooldownCanAutoRecover() {
            assertThat(UserAgentStatus.COOLDOWN.canAutoRecover()).isTrue();
        }

        @Test
        @DisplayName("COOLDOWN이 아니면 자동 복구 불가능하다")
        void nonCooldownCannotAutoRecover() {
            assertThat(UserAgentStatus.IDLE.canAutoRecover()).isFalse();
            assertThat(UserAgentStatus.SUSPENDED.canAutoRecover()).isFalse();
        }
    }

    @Nested
    @DisplayName("isBlocked() 테스트")
    class IsBlockedTest {

        @Test
        @DisplayName("BLOCKED이면 true를 반환한다")
        void blockedReturnsTrue() {
            assertThat(UserAgentStatus.BLOCKED.isBlocked()).isTrue();
        }

        @Test
        @DisplayName("BLOCKED가 아니면 false를 반환한다")
        void nonBlockedReturnsFalse() {
            assertThat(UserAgentStatus.IDLE.isBlocked()).isFalse();
            assertThat(UserAgentStatus.SUSPENDED.isBlocked()).isFalse();
        }
    }

    @Nested
    @DisplayName("isAvailable() / isAvailableInPool() 테스트")
    class IsAvailableTest {

        @Test
        @DisplayName("IDLE이면 활성 Pool에 포함된다")
        void idleIsAvailableInPool() {
            assertThat(UserAgentStatus.IDLE.isAvailableInPool()).isTrue();
            assertThat(UserAgentStatus.IDLE.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("SESSION_REQUIRED이면 활성 Pool에 포함된다")
        void sessionRequiredIsAvailableInPool() {
            assertThat(UserAgentStatus.SESSION_REQUIRED.isAvailableInPool()).isTrue();
            assertThat(UserAgentStatus.SESSION_REQUIRED.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED이면 활성 Pool에 포함되지 않는다")
        void suspendedIsNotAvailableInPool() {
            assertThat(UserAgentStatus.SUSPENDED.isAvailableInPool()).isFalse();
            assertThat(UserAgentStatus.SUSPENDED.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("BLOCKED이면 활성 Pool에 포함되지 않는다")
        void blockedIsNotAvailableInPool() {
            assertThat(UserAgentStatus.BLOCKED.isAvailableInPool()).isFalse();
        }
    }

    @Nested
    @DisplayName("isReady() 테스트")
    class IsReadyTest {

        @Test
        @DisplayName("IDLE이면 즉시 사용 가능하다")
        void idleIsReady() {
            assertThat(UserAgentStatus.IDLE.isReady()).isTrue();
        }

        @Test
        @DisplayName("IDLE이 아니면 즉시 사용 불가능하다")
        void nonIdleIsNotReady() {
            assertThat(UserAgentStatus.SESSION_REQUIRED.isReady()).isFalse();
            assertThat(UserAgentStatus.SUSPENDED.isReady()).isFalse();
        }
    }

    @Nested
    @DisplayName("getDescription() 테스트")
    class GetDescriptionTest {

        @Test
        @DisplayName("각 상태의 설명을 반환한다")
        void returnsDescriptions() {
            assertThat(UserAgentStatus.IDLE.getDescription()).isEqualTo("대기 중");
            assertThat(UserAgentStatus.BORROWED.getDescription()).isEqualTo("사용 중");
            assertThat(UserAgentStatus.COOLDOWN.getDescription()).isEqualTo("쿨다운 대기");
            assertThat(UserAgentStatus.SESSION_REQUIRED.getDescription()).isEqualTo("세션 발급 필요");
            assertThat(UserAgentStatus.SUSPENDED.getDescription()).isEqualTo("일시 정지");
            assertThat(UserAgentStatus.BLOCKED.getDescription()).isEqualTo("영구 차단");
        }
    }
}
