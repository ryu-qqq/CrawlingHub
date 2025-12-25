package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response;

/**
 * Circuit Breaker 리셋 API Response
 *
 * <p>Circuit Breaker 리셋 API 응답 DTO
 *
 * @param recoveredCount 복구된 UserAgent 수
 * @param message 결과 메시지
 * @author development-team
 * @since 1.0.0
 */
public record CircuitBreakerResetApiResponse(int recoveredCount, String message) {

    public static CircuitBreakerResetApiResponse of(int recoveredCount) {
        String message =
                recoveredCount > 0
                        ? String.format(
                                "Circuit Breaker reset completed. %d user agents recovered.",
                                recoveredCount)
                        : "No suspended user agents to recover.";
        return new CircuitBreakerResetApiResponse(recoveredCount, message);
    }
}
