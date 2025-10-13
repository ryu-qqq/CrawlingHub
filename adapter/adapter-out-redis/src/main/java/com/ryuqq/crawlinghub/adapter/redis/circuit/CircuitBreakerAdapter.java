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

    @Override
    public boolean allowRequest(Long userAgentId) {
        return circuitBreakerManager.allowRequest(userAgentId);
    }

    @Override
    public void recordSuccess(Long userAgentId) {
        circuitBreakerManager.recordSuccess(userAgentId);
    }

    @Override
    public void recordFailure(Long userAgentId) {
        circuitBreakerManager.recordFailure(userAgentId);
    }

    @Override
    public void reset(Long userAgentId, String reason) {
        circuitBreakerManager.reset(userAgentId, reason);
    }
}
