package com.ryuqq.crawlinghub.application.token.port;

/**
 * Circuit Breaker Port (Outbound)
 *
 * @author crawlinghub
 */
public interface CircuitBreakerPort {

    /**
     * Circuit Breaker OPEN 상태 확인
     *
     * @param userAgentId User-Agent ID
     * @return OPEN 상태 여부
     */
    boolean isOpen(Long userAgentId);
}
