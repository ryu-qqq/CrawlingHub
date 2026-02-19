package com.ryuqq.crawlinghub.application.useragent.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentIdFixture;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecordUserAgentResultCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentPoolManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: CacheManager/ReadManager/TransactionManager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolManager 테스트")
class UserAgentPoolManagerTest {

    @Mock private UserAgentPoolCacheManager cacheManager;

    @Mock private UserAgentReadManager readManager;

    @Mock private UserAgentTransactionManager transactionManager;

    @Mock private TimeProvider timeProvider;

    @InjectMocks private UserAgentPoolManager manager;

    @BeforeEach
    void setUp() {
        java.time.Instant fixedInstant = FixedClock.aDefaultClock().instant();
        lenient().when(timeProvider.now()).thenReturn(fixedInstant);
    }

    @Nested
    @DisplayName("consume() 테스트")
    class Consume {

        @Test
        @DisplayName("[성공] 토큰 소비 → CachedUserAgent 반환")
        void shouldConsumeTokenSuccessfully() {
            // Given
            PoolStats healthyStats = new PoolStats(100, 80, 20, 85.0, 70, 100);
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            CachedUserAgent cachedUserAgent = CachedUserAgent.forNew(userAgent);

            given(cacheManager.getPoolStats()).willReturn(healthyStats);
            given(cacheManager.consumeToken()).willReturn(Optional.of(cachedUserAgent));

            // When
            CachedUserAgent result = manager.consume();

            // Then
            assertThat(result).isEqualTo(cachedUserAgent);
            verify(cacheManager).getPoolStats();
            verify(cacheManager).consumeToken();
        }

        @Test
        @DisplayName("[실패] 가용률 부족 → CircuitBreakerOpenException")
        void shouldThrowCircuitBreakerOpenWhenLowAvailability() {
            // Given
            PoolStats lowStats = new PoolStats(100, 10, 90, 10.0, 30, 70); // 10% 가용률

            given(cacheManager.getPoolStats()).willReturn(lowStats);

            // When & Then
            assertThatThrownBy(() -> manager.consume())
                    .isInstanceOf(CircuitBreakerOpenException.class);
        }

        @Test
        @DisplayName("[실패] Pool 비어있음 → CircuitBreakerOpenException")
        void shouldThrowCircuitBreakerOpenWhenPoolEmpty() {
            // Given
            PoolStats emptyStats = PoolStats.empty();

            given(cacheManager.getPoolStats()).willReturn(emptyStats);

            // When & Then
            assertThatThrownBy(() -> manager.consume())
                    .isInstanceOf(CircuitBreakerOpenException.class);
        }

        @Test
        @DisplayName("[실패] 사용 가능한 UserAgent 없음 → NoAvailableUserAgentException")
        void shouldThrowNoAvailableUserAgentException() {
            // Given
            PoolStats healthyStats = new PoolStats(100, 80, 20, 85.0, 70, 100);

            given(cacheManager.getPoolStats()).willReturn(healthyStats);
            given(cacheManager.consumeToken()).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> manager.consume())
                    .isInstanceOf(NoAvailableUserAgentException.class);
        }
    }

    @Nested
    @DisplayName("recordResult() 테스트")
    class RecordResult {

        @Test
        @DisplayName("[성공] 성공 결과 기록")
        void shouldRecordSuccess() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.success(1L);

            // When
            manager.recordResult(command);

            // Then
            verify(cacheManager).recordSuccess(UserAgentId.of(1L));
        }

        @Test
        @DisplayName("[성공] 429 Rate Limited 처리")
        void shouldHandleRateLimited() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.failure(1L, 429);
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            given(readManager.findById(any(UserAgentId.class))).willReturn(Optional.of(userAgent));

            // When
            manager.recordResult(command);

            // Then
            verify(cacheManager).expireSession(any(UserAgentId.class));
            verify(cacheManager).removeFromPool(any(UserAgentId.class));
            verify(readManager).findById(any(UserAgentId.class));
            verify(transactionManager).persist(userAgent);
        }

        @Test
        @DisplayName("[성공] 일반 실패 처리 (SUSPENDED 되지 않음)")
        void shouldHandleFailureNotSuspended() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.failure(1L, 500);

            given(cacheManager.recordFailure(any(UserAgentId.class), any(Integer.class)))
                    .willReturn(false);

            // When
            manager.recordResult(command);

            // Then
            verify(cacheManager).recordFailure(any(UserAgentId.class), any(Integer.class));
            verify(readManager, never()).findById(any(UserAgentId.class));
            verify(transactionManager, never()).persist(any(UserAgent.class));
        }

        @Test
        @DisplayName("[성공] 일반 실패 처리 (SUSPENDED 됨)")
        void shouldHandleFailureSuspended() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.failure(1L, 500);
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            given(cacheManager.recordFailure(any(UserAgentId.class), any(Integer.class)))
                    .willReturn(true);
            given(readManager.findById(any(UserAgentId.class))).willReturn(Optional.of(userAgent));

            // When
            manager.recordResult(command);

            // Then
            verify(readManager).findById(any(UserAgentId.class));
            verify(transactionManager).persist(userAgent);
        }
    }

    @Nested
    @DisplayName("recoverSuspendedUserAgents() 테스트")
    class RecoverSuspendedUserAgents {

        @Test
        @DisplayName("[성공] 복구 대상 없음 → 0 반환")
        void shouldReturnZeroWhenNoRecoverableAgents() {
            // Given
            given(cacheManager.getRecoverableUserAgents()).willReturn(List.of());

            // When
            int result = manager.recoverSuspendedUserAgents();

            // Then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("[성공] 복구 대상 있음 → 복구 수행")
        void shouldRecoverSuspendedAgents() {
            // Given
            UserAgentId userAgentId = UserAgentIdFixture.anAssignedId();
            UserAgent userAgent = UserAgentFixture.aSuspendedUserAgent();

            given(cacheManager.getRecoverableUserAgents()).willReturn(List.of(userAgentId));
            given(readManager.findById(userAgentId)).willReturn(Optional.of(userAgent));

            // When
            int result = manager.recoverSuspendedUserAgents();

            // Then
            assertThat(result).isEqualTo(1);
            verify(cacheManager).restoreToPool(any(UserAgentId.class), any(String.class));
            verify(transactionManager).persist(userAgent);
        }

        @Test
        @DisplayName("[성공] 복구 대상 조회 실패 → 0 반환")
        void shouldReturnZeroWhenQueryFails() {
            // Given
            UserAgentId userAgentId = UserAgentIdFixture.anAssignedId();

            given(cacheManager.getRecoverableUserAgents()).willReturn(List.of(userAgentId));
            given(readManager.findById(userAgentId)).willReturn(Optional.empty());

            // When
            int result = manager.recoverSuspendedUserAgents();

            // Then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("getPoolStats() 테스트")
    class GetPoolStats {

        @Test
        @DisplayName("[성공] Pool 상태 조회")
        void shouldReturnPoolStats() {
            // Given
            PoolStats expectedStats = new PoolStats(100, 80, 20, 85.0, 70, 100);

            given(cacheManager.getPoolStats()).willReturn(expectedStats);

            // When
            PoolStats result = manager.getPoolStats();

            // Then
            assertThat(result).isEqualTo(expectedStats);
            assertThat(result.total()).isEqualTo(100);
            assertThat(result.available()).isEqualTo(80);
        }
    }
}
