package com.ryuqq.crawlinghub.application.useragent.dto.response;

/**
 * Circuit Breaker 상태 응답 DTO
 *
 * <p>UserAgent Pool의 Circuit Breaker 상태를 나타냅니다.
 *
 * @param state 현재 상태 (OPEN/CLOSED)
 * @param availableRate 현재 가용률 (%)
 * @param threshold 임계치 (%)
 * @param totalAgents 전체 UserAgent 수
 * @param availableAgents 사용 가능한 UserAgent 수
 * @param suspendedAgents 정지된 UserAgent 수
 * @param canReset 리셋 가능 여부 (OPEN 상태일 때만 true)
 * @author development-team
 * @since 1.0.0
 */
public record CircuitBreakerStatusResponse(
        CircuitBreakerState state,
        double availableRate,
        double threshold,
        long totalAgents,
        long availableAgents,
        long suspendedAgents,
        boolean canReset) {

    /** Circuit Breaker 상태 */
    public enum CircuitBreakerState {
        /** 정상 상태 - 요청 처리 가능 */
        CLOSED,
        /** 열림 상태 - 요청 차단 중 */
        OPEN
    }

    /**
     * CircuitBreakerStatusResponse 생성
     *
     * @param availableRate 현재 가용률
     * @param threshold 임계치
     * @param totalAgents 전체 UserAgent 수
     * @param availableAgents 사용 가능한 UserAgent 수
     * @param suspendedAgents 정지된 UserAgent 수
     * @return CircuitBreakerStatusResponse
     */
    public static CircuitBreakerStatusResponse of(
            double availableRate,
            double threshold,
            long totalAgents,
            long availableAgents,
            long suspendedAgents) {
        CircuitBreakerState state =
                (totalAgents == 0 || availableRate < threshold)
                        ? CircuitBreakerState.OPEN
                        : CircuitBreakerState.CLOSED;
        boolean canReset = state == CircuitBreakerState.OPEN && suspendedAgents > 0;
        return new CircuitBreakerStatusResponse(
                state,
                availableRate,
                threshold,
                totalAgents,
                availableAgents,
                suspendedAgents,
                canReset);
    }
}
