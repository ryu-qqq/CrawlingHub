package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

/**
 * UserAgent Metrics API Response
 *
 * <p>UserAgent Metrics 조회 API 응답 DTO
 *
 * @param poolStats Pool 통계
 * @param healthScoreDistribution Health Score 분포
 * @param circuitBreakerStatus Circuit Breaker 상태
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentMetricsApiResponse(
        PoolStatsApiResponse poolStats,
        HealthScoreDistributionApiResponse healthScoreDistribution,
        CircuitBreakerStatusApiResponse circuitBreakerStatus) {

    /**
     * Pool 통계 API Response
     *
     * @param total 전체 UserAgent 수
     * @param available 사용 가능한 UserAgent 수
     * @param suspended 정지된 UserAgent 수
     * @param blocked 차단된 UserAgent 수
     * @param availableRate 가용률 (%)
     */
    public record PoolStatsApiResponse(
            long total, long available, long suspended, long blocked, double availableRate) {}

    /**
     * Health Score 분포 API Response
     *
     * @param avg 평균 Health Score
     * @param min 최소 Health Score
     * @param max 최대 Health Score
     * @param healthyCount Health Score >= 70인 UserAgent 수
     * @param warningCount Health Score 30-69인 UserAgent 수
     * @param criticalCount Health Score < 30인 UserAgent 수
     */
    public record HealthScoreDistributionApiResponse(
            double avg, int min, int max, int healthyCount, int warningCount, int criticalCount) {}
}
