package com.ryuqq.crawlinghub.application.useragent.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetUserAgentPoolStatusService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserAgentPoolStatusService 테스트")
class GetUserAgentPoolStatusServiceTest {

    @Mock
    private UserAgentPoolManager poolManager;

    @InjectMocks
    private GetUserAgentPoolStatusService service;

    @Nested
    @DisplayName("execute() Pool 상태 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 건강한 Pool 상태 조회")
        void shouldReturnHealthyPoolStatus() {
            // Given
            PoolStats stats = new PoolStats(10, 8, 2, 85.5, 70, 100);

            given(poolManager.getPoolStats()).willReturn(stats);

            // When
            UserAgentPoolStatusResponse result = service.execute();

            // Then
            assertThat(result.totalAgents()).isEqualTo(10);
            assertThat(result.availableAgents()).isEqualTo(8);
            assertThat(result.suspendedAgents()).isEqualTo(2);
            assertThat(result.availableRate()).isEqualTo(80.0);
            assertThat(result.isHealthy()).isTrue();
            assertThat(result.isCircuitBreakerOpen()).isFalse();
            then(poolManager).should().getPoolStats();
        }

        @Test
        @DisplayName("[성공] Circuit Breaker가 열린 Pool 상태 조회 (가용률 < 20%)")
        void shouldReturnCircuitBreakerOpenStatus() {
            // Given
            PoolStats stats = new PoolStats(10, 1, 9, 30.0, 20, 40);

            given(poolManager.getPoolStats()).willReturn(stats);

            // When
            UserAgentPoolStatusResponse result = service.execute();

            // Then
            assertThat(result.availableRate()).isEqualTo(10.0);
            assertThat(result.isCircuitBreakerOpen()).isTrue();
            assertThat(result.isHealthy()).isFalse();
        }

        @Test
        @DisplayName("[성공] 빈 Pool 상태 조회")
        void shouldReturnEmptyPoolStatus() {
            // Given
            PoolStats stats = PoolStats.empty();

            given(poolManager.getPoolStats()).willReturn(stats);

            // When
            UserAgentPoolStatusResponse result = service.execute();

            // Then
            assertThat(result.totalAgents()).isZero();
            assertThat(result.availableAgents()).isZero();
            assertThat(result.suspendedAgents()).isZero();
            assertThat(result.availableRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("[성공] Health Score 통계 포함")
        void shouldIncludeHealthScoreStats() {
            // Given
            PoolStats stats = new PoolStats(5, 4, 1, 92.0, 80, 100);

            given(poolManager.getPoolStats()).willReturn(stats);

            // When
            UserAgentPoolStatusResponse result = service.execute();

            // Then
            assertThat(result.healthScoreStats().avg()).isEqualTo(92.0);
            assertThat(result.healthScoreStats().min()).isEqualTo(80);
            assertThat(result.healthScoreStats().max()).isEqualTo(100);
        }

        @Test
        @DisplayName("[성공] 모든 UserAgent가 가용 상태인 경우")
        void shouldReturnFullyAvailablePool() {
            // Given
            PoolStats stats = new PoolStats(10, 10, 0, 100.0, 100, 100);

            given(poolManager.getPoolStats()).willReturn(stats);

            // When
            UserAgentPoolStatusResponse result = service.execute();

            // Then
            assertThat(result.availableRate()).isEqualTo(100.0);
            assertThat(result.suspendedAgents()).isZero();
            assertThat(result.isHealthy()).isTrue();
        }
    }
}
