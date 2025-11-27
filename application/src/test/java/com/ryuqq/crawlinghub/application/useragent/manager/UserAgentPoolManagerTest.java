package com.ryuqq.crawlinghub.application.useragent.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecordUserAgentResultCommand;
import com.ryuqq.crawlinghub.application.useragent.port.out.cache.UserAgentPoolCachePort;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPersistencePort;
import com.ryuqq.crawlinghub.application.useragent.port.out.query.UserAgentQueryPort;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentIdFixture;
import java.util.List;
import java.util.Optional;
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
 * <p>Mockist 스타일 테스트: Cache/Query/Persistence Port Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolManager 테스트")
class UserAgentPoolManagerTest {

    @Mock
    private UserAgentPoolCachePort cachePort;

    @Mock
    private UserAgentQueryPort queryPort;

    @Mock
    private UserAgentPersistencePort persistencePort;

    @InjectMocks
    private UserAgentPoolManager manager;

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

            given(cachePort.getPoolStats()).willReturn(healthyStats);
            given(cachePort.consumeToken()).willReturn(Optional.of(cachedUserAgent));

            // When
            CachedUserAgent result = manager.consume();

            // Then
            assertThat(result).isEqualTo(cachedUserAgent);
            verify(cachePort).getPoolStats();
            verify(cachePort).consumeToken();
        }

        @Test
        @DisplayName("[실패] 가용률 부족 → CircuitBreakerOpenException")
        void shouldThrowCircuitBreakerOpenWhenLowAvailability() {
            // Given
            PoolStats lowStats = new PoolStats(100, 10, 90, 50.0, 30, 70); // 10% 가용률

            given(cachePort.getPoolStats()).willReturn(lowStats);

            // When & Then
            assertThatThrownBy(() -> manager.consume())
                    .isInstanceOf(CircuitBreakerOpenException.class);
        }

        @Test
        @DisplayName("[실패] Pool 비어있음 → CircuitBreakerOpenException")
        void shouldThrowCircuitBreakerOpenWhenPoolEmpty() {
            // Given
            PoolStats emptyStats = PoolStats.empty();

            given(cachePort.getPoolStats()).willReturn(emptyStats);

            // When & Then
            assertThatThrownBy(() -> manager.consume())
                    .isInstanceOf(CircuitBreakerOpenException.class);
        }

        @Test
        @DisplayName("[실패] 사용 가능한 UserAgent 없음 → NoAvailableUserAgentException")
        void shouldThrowNoAvailableUserAgentException() {
            // Given
            PoolStats healthyStats = new PoolStats(100, 80, 20, 85.0, 70, 100);

            given(cachePort.getPoolStats()).willReturn(healthyStats);
            given(cachePort.consumeToken()).willReturn(Optional.empty());

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
            verify(cachePort).recordSuccess(UserAgentId.of(1L));
        }

        @Test
        @DisplayName("[성공] 429 Rate Limited 처리")
        void shouldHandleRateLimited() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.failure(1L, 429);
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            given(queryPort.findById(any(UserAgentId.class))).willReturn(Optional.of(userAgent));

            // When
            manager.recordResult(command);

            // Then
            verify(cachePort).expireSession(any(UserAgentId.class));
            verify(cachePort).removeFromPool(any(UserAgentId.class));
            verify(queryPort).findById(any(UserAgentId.class));
            verify(persistencePort).persist(userAgent);
        }

        @Test
        @DisplayName("[성공] 일반 실패 처리 (SUSPENDED 되지 않음)")
        void shouldHandleFailureNotSuspended() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.failure(1L, 500);

            given(cachePort.recordFailure(any(UserAgentId.class), any(Integer.class))).willReturn(false);

            // When
            manager.recordResult(command);

            // Then
            verify(cachePort).recordFailure(any(UserAgentId.class), any(Integer.class));
            verify(queryPort, never()).findById(any(UserAgentId.class));
            verify(persistencePort, never()).persist(any(UserAgent.class));
        }

        @Test
        @DisplayName("[성공] 일반 실패 처리 (SUSPENDED 됨)")
        void shouldHandleFailureSuspended() {
            // Given
            RecordUserAgentResultCommand command = RecordUserAgentResultCommand.failure(1L, 500);
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();

            given(cachePort.recordFailure(any(UserAgentId.class), any(Integer.class))).willReturn(true);
            given(queryPort.findById(any(UserAgentId.class))).willReturn(Optional.of(userAgent));

            // When
            manager.recordResult(command);

            // Then
            verify(queryPort).findById(any(UserAgentId.class));
            verify(persistencePort).persist(userAgent);
        }
    }

    @Nested
    @DisplayName("recoverSuspendedUserAgents() 테스트")
    class RecoverSuspendedUserAgents {

        @Test
        @DisplayName("[성공] 복구 대상 없음 → 0 반환")
        void shouldReturnZeroWhenNoRecoverableAgents() {
            // Given
            given(cachePort.getRecoverableUserAgents()).willReturn(List.of());

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

            given(cachePort.getRecoverableUserAgents()).willReturn(List.of(userAgentId));
            given(queryPort.findById(userAgentId)).willReturn(Optional.of(userAgent));

            // When
            int result = manager.recoverSuspendedUserAgents();

            // Then
            assertThat(result).isEqualTo(1);
            verify(cachePort).restoreToPool(any(UserAgentId.class), any(String.class));
            verify(persistencePort).persist(userAgent);
        }

        @Test
        @DisplayName("[성공] 복구 대상 조회 실패 → 0 반환")
        void shouldReturnZeroWhenQueryFails() {
            // Given
            UserAgentId userAgentId = UserAgentIdFixture.anAssignedId();

            given(cachePort.getRecoverableUserAgents()).willReturn(List.of(userAgentId));
            given(queryPort.findById(userAgentId)).willReturn(Optional.empty());

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

            given(cachePort.getPoolStats()).willReturn(expectedStats);

            // When
            PoolStats result = manager.getPoolStats();

            // Then
            assertThat(result).isEqualTo(expectedStats);
            assertThat(result.total()).isEqualTo(100);
            assertThat(result.available()).isEqualTo(80);
        }
    }
}
