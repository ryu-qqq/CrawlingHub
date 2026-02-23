package com.ryuqq.crawlinghub.application.useragent.validator;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheQueryManager;
import com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentPoolValidator 단위 테스트
 *
 * <p>Circuit Breaker 패턴 - Pool 가용률 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolValidator 테스트")
class UserAgentPoolValidatorTest {

    @Mock private UserAgentPoolCacheQueryManager queryManager;

    @InjectMocks private UserAgentPoolValidator validator;

    @Nested
    @DisplayName("validateAvailability() 테스트")
    class ValidateAvailability {

        @Test
        @DisplayName("[성공] Pool이 충분히 가용 가능하면 예외 없음")
        void shouldNotThrowWhenPoolIsHealthy() {
            // Given - 전체 10개, 가용 5개 = 50% (임계값 20% 초과)
            PoolStats stats = new PoolStats(10, 5, 3, 1, 1, 80.0, 70, 90);
            given(queryManager.getPoolStats()).willReturn(stats);

            // When / Then
            assertThatNoException().isThrownBy(() -> validator.validateAvailability());
            then(queryManager).should().getPoolStats();
        }

        @Test
        @DisplayName("[실패] Pool이 비어있으면 CircuitBreakerOpenException 발생")
        void shouldThrowWhenPoolIsEmpty() {
            // Given
            given(queryManager.getPoolStats()).willReturn(PoolStats.empty());

            // When / Then
            assertThatThrownBy(() -> validator.validateAvailability())
                    .isInstanceOf(CircuitBreakerOpenException.class);
        }

        @Test
        @DisplayName("[실패] 가용률이 20% 미만이면 CircuitBreakerOpenException 발생")
        void shouldThrowWhenAvailableRateBelowThreshold() {
            // Given - 전체 10개, 가용 1개 = 10% (임계값 20% 미달)
            PoolStats stats = new PoolStats(10, 1, 7, 1, 1, 40.0, 20, 70);
            given(queryManager.getPoolStats()).willReturn(stats);

            // When / Then
            assertThatThrownBy(() -> validator.validateAvailability())
                    .isInstanceOf(CircuitBreakerOpenException.class);
        }

        @Test
        @DisplayName("[성공] 가용률이 정확히 20%이면 예외 없음")
        void shouldNotThrowWhenAvailableRateEqualsThreshold() {
            // Given - 전체 10개, 가용 2개 = 20% (임계값 정확히 일치 - 미만이 아니므로 통과)
            PoolStats stats = new PoolStats(10, 2, 6, 1, 1, 50.0, 30, 80);
            given(queryManager.getPoolStats()).willReturn(stats);

            // When / Then
            assertThatNoException().isThrownBy(() -> validator.validateAvailability());
        }

        @Test
        @DisplayName("[실패] 가용률이 19%면 CircuitBreakerOpenException 발생")
        void shouldThrowWhenAvailableRateIsJustBelowThreshold() {
            // Given - 전체 100개, 가용 19개 = 19%
            PoolStats stats = new PoolStats(100, 19, 60, 10, 11, 50.0, 20, 80);
            given(queryManager.getPoolStats()).willReturn(stats);

            // When / Then
            assertThatThrownBy(() -> validator.validateAvailability())
                    .isInstanceOf(CircuitBreakerOpenException.class);
        }
    }
}
