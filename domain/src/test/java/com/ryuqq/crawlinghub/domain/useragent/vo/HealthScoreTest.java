package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * HealthScore VO 정적 메서드 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("HealthScore 정적 메서드 테스트")
class HealthScoreTest {

    @Nested
    @DisplayName("RATE_LIMIT_STATUS_CODE")
    class RateLimitStatusCode {

        @Test
        @DisplayName("Rate Limit 상태 코드는 429이다")
        void shouldBe429() {
            assertThat(HealthScore.RATE_LIMIT_STATUS_CODE).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("successIncrement()")
    class SuccessIncrement {

        @Test
        @DisplayName("성공 증가량은 5를 반환한다")
        void shouldReturnFive() {
            assertThat(HealthScore.successIncrement()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("penaltyFor()")
    class PenaltyFor {

        @Test
        @DisplayName("Rate Limit (429) -> 20 반환")
        void shouldReturn20ForRateLimit() {
            assertThat(HealthScore.penaltyFor(HealthScore.RATE_LIMIT_STATUS_CODE)).isEqualTo(20);
        }

        @Test
        @DisplayName("500 Server Error -> 10 반환")
        void shouldReturn10For500() {
            assertThat(HealthScore.penaltyFor(500)).isEqualTo(10);
        }

        @Test
        @DisplayName("502 Server Error -> 10 반환")
        void shouldReturn10For502() {
            assertThat(HealthScore.penaltyFor(502)).isEqualTo(10);
        }

        @Test
        @DisplayName("503 Server Error -> 10 반환")
        void shouldReturn10For503() {
            assertThat(HealthScore.penaltyFor(503)).isEqualTo(10);
        }

        @Test
        @DisplayName("404 기타 에러 -> 5 반환")
        void shouldReturn5For404() {
            assertThat(HealthScore.penaltyFor(404)).isEqualTo(5);
        }

        @Test
        @DisplayName("400 기타 에러 -> 5 반환")
        void shouldReturn5For400() {
            assertThat(HealthScore.penaltyFor(400)).isEqualTo(5);
        }

        @Test
        @DisplayName("403 기타 에러 -> 5 반환")
        void shouldReturn5For403() {
            assertThat(HealthScore.penaltyFor(403)).isEqualTo(5);
        }
    }
}
