package com.ryuqq.crawlinghub.application.token.port;

/**
 * Circuit Breaker Port
 * <p>
 * - Redis 기반 Circuit Breaker 패턴
 * - 외부 API 장애 격리
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface CircuitBreakerPort {

    /**
     * Circuit Breaker 열림 여부 확인
     *
     * @param identifier 식별자 (User-Agent String)
     * @return 열려있으면 true
     */
    boolean isOpen(String identifier);

    /**
     * 성공 기록
     *
     * @param identifier 식별자
     */
    void recordSuccess(String identifier);

    /**
     * 실패 기록
     *
     * @param identifier 식별자
     */
    void recordFailure(String identifier);
}
