package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

/**
 * UserAgent Pool Status API Response
 *
 * <p>UserAgent Pool 상태 조회 API 응답 DTO
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>totalAgents: 전체 UserAgent 수
 *   <li>availableAgents: 사용 가능한 UserAgent 수
 *   <li>suspendedAgents: 정지된 UserAgent 수
 *   <li>availableRate: 가용률 (%)
 *   <li>healthScoreStats: Health Score 통계 (avg, min, max)
 *   <li>isCircuitBreakerOpen: Circuit Breaker 열림 여부
 *   <li>isHealthy: Pool 상태 건강 여부
 * </ul>
 *
 * @param totalAgents 전체 UserAgent 수
 * @param availableAgents 사용 가능한 UserAgent 수
 * @param suspendedAgents 정지된 UserAgent 수
 * @param availableRate 가용률 (%)
 * @param healthScoreStats Health Score 통계
 * @param isCircuitBreakerOpen Circuit Breaker 열림 여부
 * @param isHealthy Pool 상태 건강 여부
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentPoolStatusApiResponse(
        long totalAgents,
        long availableAgents,
        long suspendedAgents,
        double availableRate,
        HealthScoreStatsApiResponse healthScoreStats,
        boolean isCircuitBreakerOpen,
        boolean isHealthy) {

    /**
     * Health Score 통계 API 응답 DTO
     *
     * @param avg 평균 Health Score
     * @param min 최소 Health Score
     * @param max 최대 Health Score
     */
    public record HealthScoreStatsApiResponse(double avg, int min, int max) {}
}
