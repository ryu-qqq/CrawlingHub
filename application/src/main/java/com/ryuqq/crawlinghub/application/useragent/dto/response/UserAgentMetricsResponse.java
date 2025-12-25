package com.ryuqq.crawlinghub.application.useragent.dto.response;

/**
 * UserAgent Metrics 응답 DTO
 *
 * <p>UserAgent Pool의 집계 메트릭을 나타냅니다.
 *
 * @param poolStats Pool 통계
 * @param healthScoreDistribution Health Score 분포
 * @param circuitBreakerStatus Circuit Breaker 상태
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentMetricsResponse(
        PoolStats poolStats,
        HealthScoreDistribution healthScoreDistribution,
        CircuitBreakerStatusResponse circuitBreakerStatus) {

    /**
     * Pool 통계
     *
     * @param total 전체 UserAgent 수
     * @param available 사용 가능한 UserAgent 수
     * @param suspended 정지된 UserAgent 수
     * @param blocked 차단된 UserAgent 수
     * @param availableRate 가용률 (%)
     */
    public record PoolStats(
            long total, long available, long suspended, long blocked, double availableRate) {

        public static PoolStats of(long total, long available, long suspended, long blocked) {
            double rate = total > 0 ? (double) available / total * 100 : 0.0;
            return new PoolStats(total, available, suspended, blocked, rate);
        }
    }

    /**
     * Health Score 분포
     *
     * @param avg 평균 Health Score
     * @param min 최소 Health Score
     * @param max 최대 Health Score
     * @param healthyCount Health Score >= 70인 UserAgent 수
     * @param warningCount Health Score 30-69인 UserAgent 수
     * @param criticalCount Health Score < 30인 UserAgent 수
     */
    public record HealthScoreDistribution(
            double avg, int min, int max, int healthyCount, int warningCount, int criticalCount) {

        public static HealthScoreDistribution of(
                double avg,
                int min,
                int max,
                int healthyCount,
                int warningCount,
                int criticalCount) {
            return new HealthScoreDistribution(
                    avg, min, max, healthyCount, warningCount, criticalCount);
        }

        public static HealthScoreDistribution empty() {
            return new HealthScoreDistribution(0.0, 0, 0, 0, 0, 0);
        }
    }

    /**
     * UserAgentMetricsResponse 생성
     *
     * @param poolStats Pool 통계
     * @param healthScoreDistribution Health Score 분포
     * @param circuitBreakerStatus Circuit Breaker 상태
     * @return UserAgentMetricsResponse
     */
    public static UserAgentMetricsResponse of(
            PoolStats poolStats,
            HealthScoreDistribution healthScoreDistribution,
            CircuitBreakerStatusResponse circuitBreakerStatus) {
        return new UserAgentMetricsResponse(
                poolStats, healthScoreDistribution, circuitBreakerStatus);
    }
}
