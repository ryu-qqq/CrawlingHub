package com.ryuqq.crawlinghub.domain.useragent.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentStateException;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import com.ryuqq.cralwinghub.domain.fixture.useragent.HealthScoreFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.TokenFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentIdFixture;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * UserAgent Aggregate Root 단위 테스트
 *
 * <p>테스트 대상:
 * <ul>
 *   <li>신규 생성 {@code create()} - AVAILABLE 상태, Health Score 100</li>
 *   <li>복원 {@code reconstitute()} - 기존 데이터 복원</li>
 *   <li>사용 기록 {@code markAsUsed()} - lastUsedAt, requestsPerDay 업데이트</li>
 *   <li>성공 기록 {@code recordSuccess()} - Health Score +5</li>
 *   <li>실패 기록 {@code recordFailure()} - HTTP 상태 코드 기반 페널티</li>
 *   <li>정지 {@code suspend()} - AVAILABLE → SUSPENDED</li>
 *   <li>복구 {@code recover()} - SUSPENDED → AVAILABLE, Health Score 70</li>
 *   <li>차단 {@code block()} - BLOCKED 상태</li>
 *   <li>상태 확인 메서드 - isAvailable, isSuspended, isBlocked, isRecoverable</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("UserAgent Aggregate Root 테스트")
class UserAgentTest {

    @Nested
    @DisplayName("create() 신규 생성 테스트")
    class Create {

        @Test
        @DisplayName("신규 UserAgent 생성 시 AVAILABLE 상태, Health Score 100, ID 미할당")
        void shouldCreateUserAgentWithAvailableStatusAndFullHealthScore() {
            // when
            UserAgent userAgent = UserAgent.create(TokenFixture.aDefaultToken());

            // then
            assertThat(userAgent.getId()).isNotNull();
            assertThat(userAgent.getId().value()).isNull();
            assertThat(userAgent.getToken()).isEqualTo(TokenFixture.aDefaultToken());
            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.AVAILABLE);
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(100);
            assertThat(userAgent.isAvailable()).isTrue();
            assertThat(userAgent.getRequestsPerDay()).isZero();
            assertThat(userAgent.getLastUsedAt()).isNull();
        }

        @Test
        @DisplayName("신규 UserAgent 생성 시 createdAt, updatedAt 설정")
        void shouldSetTimestamps() {
            // given
            LocalDateTime before = LocalDateTime.now();

            // when
            UserAgent userAgent = UserAgent.create(TokenFixture.aDefaultToken());

            // then
            LocalDateTime after = LocalDateTime.now();
            assertThat(userAgent.getCreatedAt()).isNotNull();
            assertThat(userAgent.getUpdatedAt()).isNotNull();
            assertThat(userAgent.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(userAgent.getCreatedAt()).isBeforeOrEqualTo(after);
        }
    }

    @Nested
    @DisplayName("reconstitute() 영속성 복원 테스트")
    class Reconstitute {

        @Test
        @DisplayName("기존 데이터로 UserAgent 복원 성공")
        void shouldRestoreUserAgentFromExistingData() {
            // when
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // then
            assertThat(userAgent.getId().value()).isNotNull();
            assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.AVAILABLE);
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(100);
        }

        @Test
        @DisplayName("SUSPENDED 상태로 UserAgent 복원")
        void shouldRestoreSuspendedUserAgent() {
            // when
            UserAgent userAgent = UserAgentFixture.aSuspendedUserAgent();

            // then
            assertThat(userAgent.isSuspended()).isTrue();
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(29);
        }

        @Test
        @DisplayName("BLOCKED 상태로 UserAgent 복원")
        void shouldRestoreBlockedUserAgent() {
            // when
            UserAgent userAgent = UserAgentFixture.aBlockedUserAgent();

            // then
            assertThat(userAgent.isBlocked()).isTrue();
            assertThat(userAgent.getHealthScoreValue()).isZero();
        }
    }

    @Nested
    @DisplayName("markAsUsed() 사용 기록 테스트")
    class MarkAsUsed {

        @Test
        @DisplayName("사용 시 lastUsedAt 업데이트")
        void shouldUpdateLastUsedAt() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            assertThat(userAgent.getLastUsedAt()).isNotNull();
            LocalDateTime originalLastUsedAt = userAgent.getLastUsedAt();

            // when
            userAgent.markAsUsed();

            // then
            assertThat(userAgent.getLastUsedAt()).isAfter(originalLastUsedAt);
        }

        @Test
        @DisplayName("사용 시 requestsPerDay 증가")
        void shouldIncrementRequestsPerDay() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            int originalRequests = userAgent.getRequestsPerDay();

            // when
            userAgent.markAsUsed();

            // then
            assertThat(userAgent.getRequestsPerDay()).isEqualTo(originalRequests + 1);
        }
    }

    @Nested
    @DisplayName("recordSuccess() 성공 기록 테스트")
    class RecordSuccess {

        @Test
        @DisplayName("성공 시 Health Score +5")
        void shouldIncreaseHealthScoreByFive() {
            // given
            UserAgent userAgent = UserAgentFixture.anAlmostSuspendedUserAgent();
            int originalScore = userAgent.getHealthScoreValue();

            // when
            userAgent.recordSuccess();

            // then
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(originalScore + 5);
        }

        @Test
        @DisplayName("Health Score는 100을 초과하지 않음")
        void shouldNotExceedMaxHealthScore() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(100);

            // when
            userAgent.recordSuccess();

            // then
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("recordFailure() 실패 기록 테스트")
    class RecordFailure {

        @Test
        @DisplayName("429 응답 시 Health Score -20, 즉시 SUSPENDED")
        void shouldApplyRateLimitPenaltyAndSuspend() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // when
            userAgent.recordFailure(429);

            // then
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(80);
            assertThat(userAgent.isSuspended()).isTrue();
        }

        @Test
        @DisplayName("5xx 응답 시 Health Score -10")
        void shouldApplyServerErrorPenalty() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // when
            userAgent.recordFailure(500);

            // then
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(90);
            assertThat(userAgent.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("5xx 응답으로 Health Score < 30이 되면 자동 SUSPENDED")
        void shouldAutoSuspendWhenHealthScoreBelowThreshold() {
            // given
            UserAgent userAgent = UserAgentFixture.anAlmostSuspendedUserAgent();
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(35);

            // when
            userAgent.recordFailure(500); // -10 → 25

            // then
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(25);
            assertThat(userAgent.isSuspended()).isTrue();
        }

        @Test
        @DisplayName("기타 응답 시 Health Score -5")
        void shouldApplyOtherErrorPenalty() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // when
            userAgent.recordFailure(400);

            // then
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(95);
            assertThat(userAgent.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("기타 응답으로 Health Score < 30이 되면 자동 SUSPENDED")
        void shouldAutoSuspendWhenHealthScoreBelowThresholdByOtherError() {
            // given
            UserAgent userAgent = UserAgent.reconstitute(
                    UserAgentIdFixture.anAssignedId(),
                    TokenFixture.aDefaultToken(),
                    UserAgentStatus.AVAILABLE,
                    HealthScoreFixture.of(32),
                    LocalDateTime.now(),
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now());

            // when
            userAgent.recordFailure(400); // -5 → 27

            // then
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(27);
            assertThat(userAgent.isSuspended()).isTrue();
        }
    }

    @Nested
    @DisplayName("suspend() 수동 정지 테스트")
    class Suspend {

        @Test
        @DisplayName("AVAILABLE → SUSPENDED 전환 성공")
        void shouldTransitionFromAvailableToSuspended() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // when
            userAgent.suspend();

            // then
            assertThat(userAgent.isSuspended()).isTrue();
            assertThat(userAgent.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("이미 SUSPENDED 상태에서 suspend() 호출 시 예외 발생")
        void shouldThrowExceptionWhenAlreadySuspended() {
            // given
            UserAgent userAgent = UserAgentFixture.aSuspendedUserAgent();

            // when & then
            assertThatThrownBy(userAgent::suspend)
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }

        @Test
        @DisplayName("BLOCKED 상태에서 suspend() 호출 시 예외 발생")
        void shouldThrowExceptionWhenBlocked() {
            // given
            UserAgent userAgent = UserAgentFixture.aBlockedUserAgent();

            // when & then
            assertThatThrownBy(userAgent::suspend)
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }
    }

    @Nested
    @DisplayName("recover() 복구 테스트")
    class Recover {

        @Test
        @DisplayName("SUSPENDED → AVAILABLE 전환 성공, Health Score 70")
        void shouldTransitionFromSuspendedToAvailable() {
            // given
            UserAgent userAgent = UserAgentFixture.aSuspendedUserAgent();
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(29);

            // when
            userAgent.recover();

            // then
            assertThat(userAgent.isAvailable()).isTrue();
            assertThat(userAgent.isSuspended()).isFalse();
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(70);
        }

        @Test
        @DisplayName("AVAILABLE 상태에서 recover() 호출 시 예외 발생")
        void shouldThrowExceptionWhenAlreadyAvailable() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // when & then
            assertThatThrownBy(userAgent::recover)
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }

        @Test
        @DisplayName("BLOCKED 상태에서 recover() 호출 시 예외 발생")
        void shouldThrowExceptionWhenBlocked() {
            // given
            UserAgent userAgent = UserAgentFixture.aBlockedUserAgent();

            // when & then
            assertThatThrownBy(userAgent::recover)
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }
    }

    @Nested
    @DisplayName("block() 영구 차단 테스트")
    class Block {

        @Test
        @DisplayName("AVAILABLE → BLOCKED 전환 성공")
        void shouldTransitionFromAvailableToBlocked() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // when
            userAgent.block();

            // then
            assertThat(userAgent.isBlocked()).isTrue();
            assertThat(userAgent.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("SUSPENDED → BLOCKED 전환 성공")
        void shouldTransitionFromSuspendedToBlocked() {
            // given
            UserAgent userAgent = UserAgentFixture.aSuspendedUserAgent();

            // when
            userAgent.block();

            // then
            assertThat(userAgent.isBlocked()).isTrue();
            assertThat(userAgent.isSuspended()).isFalse();
        }

        @Test
        @DisplayName("이미 BLOCKED 상태에서 block() 호출 시 예외 발생")
        void shouldThrowExceptionWhenAlreadyBlocked() {
            // given
            UserAgent userAgent = UserAgentFixture.aBlockedUserAgent();

            // when & then
            assertThatThrownBy(userAgent::block)
                    .isInstanceOf(InvalidUserAgentStateException.class);
        }
    }

    @Nested
    @DisplayName("resetDailyRequests() 일일 요청 초기화 테스트")
    class ResetDailyRequests {

        @Test
        @DisplayName("requestsPerDay가 0으로 초기화")
        void shouldResetRequestsPerDayToZero() {
            // given
            UserAgent userAgent = UserAgentFixture.aHighUsageUserAgent();
            assertThat(userAgent.getRequestsPerDay()).isEqualTo(100);

            // when
            userAgent.resetDailyRequests();

            // then
            assertThat(userAgent.getRequestsPerDay()).isZero();
        }
    }

    @Nested
    @DisplayName("isRecoverable() 복구 대상 확인 테스트")
    class IsRecoverable {

        @Test
        @DisplayName("SUSPENDED + lastUsedAt < threshold 이면 복구 대상")
        void shouldReturnTrueWhenSuspendedAndOldEnough() {
            // given
            UserAgent userAgent = UserAgentFixture.aRecoverableSuspendedUserAgent();
            LocalDateTime threshold = LocalDateTime.now().minusHours(1);

            // when
            boolean result = userAgent.isRecoverable(threshold);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED + lastUsedAt >= threshold 이면 복구 대상 아님")
        void shouldReturnFalseWhenSuspendedButNotOldEnough() {
            // given
            UserAgent userAgent = UserAgentFixture.aSuspendedUserAgent();
            // aSuspendedUserAgent의 lastUsedAt = DEFAULT_TIME (2025-11-27 12:00:00)
            // threshold를 lastUsedAt 이전으로 설정하면 lastUsedAt >= threshold가 됨
            LocalDateTime threshold = LocalDateTime.of(2024, 1, 1, 11, 0, 0);

            // when
            boolean result = userAgent.isRecoverable(threshold);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("AVAILABLE 상태는 복구 대상 아님")
        void shouldReturnFalseWhenAvailable() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            LocalDateTime threshold = LocalDateTime.now();

            // when
            boolean result = userAgent.isRecoverable(threshold);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("BLOCKED 상태는 복구 대상 아님")
        void shouldReturnFalseWhenBlocked() {
            // given
            UserAgent userAgent = UserAgentFixture.aBlockedUserAgent();
            LocalDateTime threshold = LocalDateTime.now();

            // when
            boolean result = userAgent.isRecoverable(threshold);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드 테스트")
    class StatusChecks {

        @Test
        @DisplayName("isAvailable()은 AVAILABLE 상태일 때 true 반환")
        void shouldReturnTrueForIsAvailableWhenAvailable() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // then
            assertThat(userAgent.isAvailable()).isTrue();
            assertThat(userAgent.isSuspended()).isFalse();
            assertThat(userAgent.isBlocked()).isFalse();
        }

        @Test
        @DisplayName("isSuspended()는 SUSPENDED 상태일 때 true 반환")
        void shouldReturnTrueForIsSuspendedWhenSuspended() {
            // given
            UserAgent userAgent = UserAgentFixture.aSuspendedUserAgent();

            // then
            assertThat(userAgent.isSuspended()).isTrue();
            assertThat(userAgent.isAvailable()).isFalse();
            assertThat(userAgent.isBlocked()).isFalse();
        }

        @Test
        @DisplayName("isBlocked()는 BLOCKED 상태일 때 true 반환")
        void shouldReturnTrueForIsBlockedWhenBlocked() {
            // given
            UserAgent userAgent = UserAgentFixture.aBlockedUserAgent();

            // then
            assertThat(userAgent.isBlocked()).isTrue();
            assertThat(userAgent.isAvailable()).isFalse();
            assertThat(userAgent.isSuspended()).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter 메서드 테스트")
    class GetterMethods {

        @Test
        @DisplayName("getId()는 UserAgentId 반환")
        void shouldReturnUserAgentId() {
            // given
            UserAgent userAgent = UserAgentFixture.anUserAgentWithId(123L);

            // then
            assertThat(userAgent.getId().value()).isEqualTo(123L);
        }

        @Test
        @DisplayName("getToken()은 Token 반환")
        void shouldReturnToken() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // then
            assertThat(userAgent.getToken()).isEqualTo(TokenFixture.aDefaultToken());
        }

        @Test
        @DisplayName("getHealthScore()는 HealthScore 반환")
        void shouldReturnHealthScore() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // then
            assertThat(userAgent.getHealthScore()).isEqualTo(HealthScoreFixture.initial());
        }

        @Test
        @DisplayName("getHealthScoreValue()는 정수 값 반환")
        void shouldReturnHealthScoreValue() {
            // given
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            // then
            assertThat(userAgent.getHealthScoreValue()).isEqualTo(100);
        }
    }
}
