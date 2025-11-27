package com.ryuqq.crawlinghub.application.useragent.dto.response;

/**
 * UserAgent Pool 상태 응답 DTO
 *
 * <p>Pool의 전체적인 상태를 모니터링하기 위한 응답 객체입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentPoolStatusResponse(
        long totalAgents,
        long availableAgents,
        long suspendedAgents,
        double availableRate,
        HealthScoreStats healthScoreStats) {
    private static final double CIRCUIT_BREAKER_THRESHOLD = 20.0;

    /** Health Score 통계 정보 */
    public record HealthScoreStats(double avg, int min, int max) {
        /** 빈 통계 (UserAgent가 없을 때) */
        public static HealthScoreStats empty() {
            return new HealthScoreStats(0.0, 0, 0);
        }
    }

    /**
     * Pool 상태 응답 생성
     *
     * @param totalAgents 전체 UserAgent 수
     * @param availableAgents 사용 가능한 UserAgent 수
     * @param suspendedAgents 정지된 UserAgent 수
     * @param healthScoreStats Health Score 통계
     * @return UserAgentPoolStatusResponse
     */
    public static UserAgentPoolStatusResponse of(
            long totalAgents,
            long availableAgents,
            long suspendedAgents,
            HealthScoreStats healthScoreStats) {
        double rate = totalAgents > 0 ? (double) availableAgents / totalAgents * 100 : 0.0;

        return new UserAgentPoolStatusResponse(
                totalAgents, availableAgents, suspendedAgents, rate, healthScoreStats);
    }

    /**
     * Circuit Breaker가 열려야 하는 상태인지 확인
     *
     * @return 가용률 < 20%이면 true
     */
    public boolean isCircuitBreakerOpen() {
        return availableRate < CIRCUIT_BREAKER_THRESHOLD;
    }

    /**
     * Pool 상태가 건강한지 확인
     *
     * @return 가용률 >= 50%이면 true
     */
    public boolean isHealthy() {
        return availableRate >= 50.0;
    }
}
