package com.ryuqq.crawlinghub.application.execution.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheStateManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.validator.UserAgentPoolValidator;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlingUserAgentCoordinator 테스트")
class CrawlingUserAgentCoordinatorTest {

    @Mock private UserAgentPoolValidator poolValidator;
    @Mock private UserAgentPoolCacheCommandManager cacheCommandManager;
    @Mock private UserAgentPoolCacheStateManager cacheStateManager;
    @Mock private UserAgentReadManager readManager;
    @Mock private UserAgentCommandManager commandManager;
    @InjectMocks private CrawlingUserAgentCoordinator coordinator;

    @Nested
    @DisplayName("consume() 테스트")
    class Consume {

        @Test
        @DisplayName("[성공] Redis 정상 -> CachedUserAgent 반환")
        void shouldConsumeFromRedis() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            CachedUserAgent cached = CachedUserAgent.forNew(userAgent);
            given(cacheCommandManager.consumeToken()).willReturn(Optional.of(cached));

            CachedUserAgent result = coordinator.consume();

            assertThat(result).isEqualTo(cached);
            verify(poolValidator).validateAvailability();
        }

        @Test
        @DisplayName("[폴백] Redis 장애 -> DB에서 healthScore 최고 UserAgent 선택")
        void shouldFallbackToDbWhenRedisDown() {
            willThrow(new RuntimeException("Redis down")).given(cacheCommandManager).consumeToken();
            UserAgent low = UserAgentFixture.anAvailableUserAgent(1L, 50);
            UserAgent high = UserAgentFixture.anAvailableUserAgent(2L, 90);
            given(readManager.findAllAvailable()).willReturn(List.of(low, high));

            CachedUserAgent result = coordinator.consume();

            assertThat(result.userAgentId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("[실패] CircuitBreakerOpenException -> 그대로 전파")
        void shouldPropagateCircuitBreakerOpen() {
            willThrow(new CircuitBreakerOpenException(15.0))
                    .given(poolValidator)
                    .validateAvailability();

            assertThatThrownBy(() -> coordinator.consume())
                    .isInstanceOf(CircuitBreakerOpenException.class);
        }

        @Test
        @DisplayName("[실패] Redis 정상 + 토큰 없음 -> NoAvailableUserAgentException 전파")
        void shouldPropagateNoAvailableUserAgent() {
            given(cacheCommandManager.consumeToken()).willReturn(Optional.empty());

            assertThatThrownBy(() -> coordinator.consume())
                    .isInstanceOf(NoAvailableUserAgentException.class);
        }

        @Test
        @DisplayName("[실패] Redis 장애 + DB에도 없음 -> NoAvailableUserAgentException")
        void shouldThrowWhenDbAlsoEmpty() {
            willThrow(new RuntimeException("Redis down")).given(cacheCommandManager).consumeToken();
            given(readManager.findAllAvailable()).willReturn(List.of());

            assertThatThrownBy(() -> coordinator.consume())
                    .isInstanceOf(NoAvailableUserAgentException.class);
        }
    }

    @Nested
    @DisplayName("recordResult() - 성공 시나리오")
    class RecordResultSuccess {

        @Test
        @DisplayName("[성공] 성공 기록 -> cacheStateManager.applyHealthDelta 호출")
        void shouldRecordSuccess() {
            coordinator.recordResult(1L, true, 200);

            verify(cacheStateManager)
                    .applyHealthDelta(UserAgentId.of(1L), HealthScore.successIncrement());
        }

        @Test
        @DisplayName("[성공] Redis recordSuccess 실패 -> 예외 없이 스킵")
        void shouldSkipWhenRedisRecordSuccessFails() {
            willThrow(new RuntimeException("Redis down"))
                    .given(cacheStateManager)
                    .applyHealthDelta(UserAgentId.of(1L), HealthScore.successIncrement());

            coordinator.recordResult(1L, true, 200);

            verify(cacheStateManager)
                    .applyHealthDelta(UserAgentId.of(1L), HealthScore.successIncrement());
        }
    }

    @Nested
    @DisplayName("recordResult() - 실패 시나리오")
    class RecordResultFailure {

        @Test
        @DisplayName("[성공] 일반 실패 + SUSPENDED -> DB 저장")
        void shouldPersistWhenSuspended() {
            UserAgentId id = UserAgentId.of(1L);
            int penalty = HealthScore.penaltyFor(500);
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            given(cacheStateManager.applyHealthDelta(id, -penalty)).willReturn(true);
            given(readManager.findById(id)).willReturn(Optional.of(userAgent));

            coordinator.recordResult(1L, false, 500);

            verify(commandManager).persist(userAgent);
        }

        @Test
        @DisplayName("[성공] 일반 실패 + 미 SUSPENDED -> DB 저장 안함")
        void shouldNotPersistWhenNotSuspended() {
            UserAgentId id = UserAgentId.of(1L);
            int penalty = HealthScore.penaltyFor(500);
            given(cacheStateManager.applyHealthDelta(id, -penalty)).willReturn(false);

            coordinator.recordResult(1L, false, 500);

            verify(commandManager, never()).persist(any());
        }

        @Test
        @DisplayName("[성공] Redis recordFailure 실패 -> suspended=false, DB 저장 안함")
        void shouldNotPersistWhenRedisRecordFailureFails() {
            UserAgentId id = UserAgentId.of(1L);
            int penalty = HealthScore.penaltyFor(500);
            willThrow(new RuntimeException("Redis down"))
                    .given(cacheStateManager)
                    .applyHealthDelta(id, -penalty);

            coordinator.recordResult(1L, false, 500);

            verify(commandManager, never()).persist(any());
        }
    }

    @Nested
    @DisplayName("recordResult() - Rate Limit 시나리오")
    class RecordResultRateLimited {

        @Test
        @DisplayName("[성공] Rate Limit -> suspendForRateLimit (atomic) + DB 저장")
        void shouldHandleRateLimited() {
            UserAgentId id = UserAgentId.of(1L);
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            given(readManager.findById(id)).willReturn(Optional.of(userAgent));

            coordinator.recordResult(1L, false, HealthScore.RATE_LIMIT_STATUS_CODE);

            verify(cacheCommandManager).suspendForRateLimit(id);
            verify(commandManager).persist(userAgent);
        }

        @Test
        @DisplayName("[성공] Rate Limit + DB에 없음 -> Redis 작업만 수행")
        void shouldHandleRateLimitedWithoutDbRecord() {
            UserAgentId id = UserAgentId.of(1L);
            given(readManager.findById(id)).willReturn(Optional.empty());

            coordinator.recordResult(1L, false, HealthScore.RATE_LIMIT_STATUS_CODE);

            verify(cacheCommandManager).suspendForRateLimit(id);
            verify(commandManager, never()).persist(any());
        }

        @Test
        @DisplayName("[성공] Rate Limit + Redis 실패 -> DB 저장 계속 진행")
        void shouldContinueWhenRedisFails() {
            UserAgentId id = UserAgentId.of(1L);
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            willThrow(new RuntimeException("Redis down"))
                    .given(cacheCommandManager)
                    .suspendForRateLimit(id);
            given(readManager.findById(id)).willReturn(Optional.of(userAgent));

            coordinator.recordResult(1L, false, HealthScore.RATE_LIMIT_STATUS_CODE);

            verify(commandManager).persist(userAgent);
        }
    }
}
