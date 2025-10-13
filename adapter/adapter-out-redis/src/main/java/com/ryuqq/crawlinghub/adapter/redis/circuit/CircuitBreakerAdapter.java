package com.ryuqq.crawlinghub.adapter.redis.circuit;

import com.ryuqq.crawlinghub.application.token.port.CircuitBreakerPort;
import org.springframework.stereotype.Component;

/**
 * Circuit Breaker Adapter
 *
 * @author crawlinghub
 */
@Component
public class CircuitBreakerAdapter implements CircuitBreakerPort {

    private final CircuitBreakerManager circuitBreakerManager;

    public CircuitBreakerAdapter(CircuitBreakerManager circuitBreakerManager) {
        this.circuitBreakerManager = circuitBreakerManager;
    }

    @Override
    public boolean isOpen(Long userAgentId) {
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        return state.getStatus() == CircuitBreakerManager.CircuitStatus.OPEN;
    }
}
