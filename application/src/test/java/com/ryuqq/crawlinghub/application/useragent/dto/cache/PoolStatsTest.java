package com.ryuqq.crawlinghub.application.useragent.dto.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * PoolStats 단위 테스트
 *
 * <p>UserAgent Pool 통계 DTO 및 가용률 계산 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("PoolStats 테스트")
class PoolStatsTest {

    @Nested
    @DisplayName("empty() 테스트")
    class Empty {

        @Test
        @DisplayName("[성공] 빈 Pool 통계 생성")
        void shouldCreateEmptyPoolStats() {
            // When
            PoolStats stats = PoolStats.empty();

            // Then
            assertThat(stats.total()).isZero();
            assertThat(stats.available()).isZero();
            assertThat(stats.borrowed()).isZero();
            assertThat(stats.cooldown()).isZero();
            assertThat(stats.suspended()).isZero();
            assertThat(stats.avgHealthScore()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("availableRate() 테스트")
    class AvailableRate {

        @Test
        @DisplayName("[성공] 전체 10개 중 5개 사용 가능하면 50%")
        void shouldReturn50WhenHalfAvailable() {
            // Given
            PoolStats stats = new PoolStats(10, 5, 3, 1, 1, 80.0, 70, 90);

            // When
            double rate = stats.availableRate();

            // Then
            assertThat(rate).isCloseTo(50.0, within(0.01));
        }

        @Test
        @DisplayName("[성공] 전체 0개이면 가용률 0%")
        void shouldReturnZeroWhenTotalIsZero() {
            // Given
            PoolStats stats = PoolStats.empty();

            // When
            double rate = stats.availableRate();

            // Then
            assertThat(rate).isEqualTo(0.0);
        }

        @Test
        @DisplayName("[성공] 전체 10개 전부 사용 가능하면 100%")
        void shouldReturn100WhenAllAvailable() {
            // Given
            PoolStats stats = new PoolStats(10, 10, 0, 0, 0, 90.0, 80, 100);

            // When
            double rate = stats.availableRate();

            // Then
            assertThat(rate).isCloseTo(100.0, within(0.01));
        }

        @Test
        @DisplayName("[성공] 전체 10개 중 2개만 가용 가능하면 20%")
        void shouldReturn20WhenTwoOfTenAvailable() {
            // Given
            PoolStats stats = new PoolStats(10, 2, 5, 2, 1, 60.0, 30, 90);

            // When
            double rate = stats.availableRate();

            // Then
            assertThat(rate).isCloseTo(20.0, within(0.01));
        }
    }
}
