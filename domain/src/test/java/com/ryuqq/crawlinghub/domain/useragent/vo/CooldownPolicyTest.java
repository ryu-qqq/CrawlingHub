package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("CooldownPolicy Value Object 단위 테스트")
class CooldownPolicyTest {

    private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    @Nested
    @DisplayName("none() 팩토리 메서드 테스트")
    class NoneTest {

        @Test
        @DisplayName("초기 상태는 카운터 0, 쿨다운 없음이다")
        void noneHasZeroCountAndNoCooldown() {
            CooldownPolicy policy = CooldownPolicy.none();
            assertThat(policy.consecutiveRateLimits()).isEqualTo(0);
            assertThat(policy.cooldownUntil()).isNull();
        }

        @Test
        @DisplayName("SUSPENDED로 에스컬레이션하지 않는다")
        void noneDoesNotEscalate() {
            CooldownPolicy policy = CooldownPolicy.none();
            assertThat(policy.shouldEscalateToSuspended()).isFalse();
        }
    }

    @Nested
    @DisplayName("escalate() 팩토리 메서드 테스트")
    class EscalateTest {

        @Test
        @DisplayName("첫 번째 에스컬레이션 시 카운터가 1이 된다")
        void firstEscalateIncreasesCountToOne() {
            CooldownPolicy policy = CooldownPolicy.escalate(0, NOW);
            assertThat(policy.consecutiveRateLimits()).isEqualTo(1);
            assertThat(policy.cooldownUntil()).isNotNull();
            assertThat(policy.cooldownUntil()).isAfter(NOW);
        }

        @Test
        @DisplayName("에스컬레이션 시마다 카운터가 증가한다")
        void eachEscalateIncreasesCount() {
            CooldownPolicy policy1 = CooldownPolicy.escalate(0, NOW);
            CooldownPolicy policy2 = CooldownPolicy.escalate(policy1.consecutiveRateLimits(), NOW);
            assertThat(policy2.consecutiveRateLimits()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("reconstitute() 팩토리 메서드 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("기존 값으로 복원한다")
        void reconstituteWithExistingValues() {
            Instant cooldownUntil = NOW.plusSeconds(3600);
            CooldownPolicy policy = CooldownPolicy.reconstitute(3, cooldownUntil);
            assertThat(policy.consecutiveRateLimits()).isEqualTo(3);
            assertThat(policy.cooldownUntil()).isEqualTo(cooldownUntil);
        }

        @Test
        @DisplayName("null cooldownUntil로 복원한다")
        void reconstituteWithNullCooldownUntil() {
            CooldownPolicy policy = CooldownPolicy.reconstitute(2, null);
            assertThat(policy.consecutiveRateLimits()).isEqualTo(2);
            assertThat(policy.cooldownUntil()).isNull();
        }
    }

    @Nested
    @DisplayName("isExpired() 테스트")
    class IsExpiredTest {

        @Test
        @DisplayName("cooldownUntil이 null이면 만료되지 않았다")
        void nullCooldownUntilIsNotExpired() {
            CooldownPolicy policy = CooldownPolicy.none();
            assertThat(policy.isExpired(NOW)).isFalse();
        }

        @Test
        @DisplayName("현재 시각이 cooldownUntil 이후이면 만료되었다")
        void afterCooldownUntilIsExpired() {
            Instant cooldownUntil = NOW.minusSeconds(1);
            CooldownPolicy policy = CooldownPolicy.reconstitute(1, cooldownUntil);
            assertThat(policy.isExpired(NOW)).isTrue();
        }

        @Test
        @DisplayName("현재 시각이 cooldownUntil 이전이면 만료되지 않았다")
        void beforeCooldownUntilIsNotExpired() {
            Instant cooldownUntil = NOW.plusSeconds(3600);
            CooldownPolicy policy = CooldownPolicy.reconstitute(1, cooldownUntil);
            assertThat(policy.isExpired(NOW)).isFalse();
        }
    }

    @Nested
    @DisplayName("shouldEscalateToSuspended() 테스트")
    class ShouldEscalateTest {

        @Test
        @DisplayName("연속 429가 5회 미만이면 에스컬레이션하지 않는다")
        void belowThresholdDoesNotEscalate() {
            CooldownPolicy policy = CooldownPolicy.reconstitute(4, NOW);
            assertThat(policy.shouldEscalateToSuspended()).isFalse();
        }

        @Test
        @DisplayName("연속 429가 5회이면 SUSPENDED로 에스컬레이션한다")
        void atThresholdEscalatesToSuspended() {
            CooldownPolicy policy = CooldownPolicy.reconstitute(5, NOW);
            assertThat(policy.shouldEscalateToSuspended()).isTrue();
        }

        @Test
        @DisplayName("연속 429가 5회 초과이면 SUSPENDED로 에스컬레이션한다")
        void aboveThresholdEscalatesToSuspended() {
            CooldownPolicy policy = CooldownPolicy.reconstitute(10, NOW);
            assertThat(policy.shouldEscalateToSuspended()).isTrue();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            Instant cooldownUntil = NOW.plusSeconds(60);
            CooldownPolicy policy1 = CooldownPolicy.reconstitute(2, cooldownUntil);
            CooldownPolicy policy2 = CooldownPolicy.reconstitute(2, cooldownUntil);
            assertThat(policy1).isEqualTo(policy2);
            assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
        }

        @Test
        @DisplayName("다른 카운터이면 다르다")
        void differentCountsAreNotEqual() {
            CooldownPolicy policy1 = CooldownPolicy.none();
            CooldownPolicy policy2 = CooldownPolicy.reconstitute(1, null);
            assertThat(policy1).isNotEqualTo(policy2);
        }
    }
}
