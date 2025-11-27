package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/**
 * Circuit Breaker Open 상태일 때 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public class CircuitBreakerOpenException extends DomainException {

    public CircuitBreakerOpenException(double availableRate) {
        super(
                UserAgentErrorCode.CIRCUIT_BREAKER_OPEN.getCode(),
                String.format("UserAgent Pool Circuit Breaker OPEN (가용률: %.2f%%)", availableRate),
                Map.of("availableRate", availableRate));
    }

    public CircuitBreakerOpenException(String message) {
        super(UserAgentErrorCode.CIRCUIT_BREAKER_OPEN.getCode(), message);
    }
}
