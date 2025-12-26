package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

/**
 * Circuit Breaker 상태 API Response
 *
 * <p>Circuit Breaker 상태 조회 API 응답 DTO
 *
 * @param state 현재 상태 (OPEN/CLOSED)
 * @param availableRate 현재 가용률 (%)
 * @param threshold 임계치 (%)
 * @param totalAgents 전체 UserAgent 수
 * @param availableAgents 사용 가능한 UserAgent 수
 * @param suspendedAgents 정지된 UserAgent 수
 * @param canReset 리셋 가능 여부
 * @author development-team
 * @since 1.0.0
 */
public record CircuitBreakerStatusApiResponse(
        String state,
        double availableRate,
        double threshold,
        long totalAgents,
        long availableAgents,
        long suspendedAgents,
        boolean canReset) {}
