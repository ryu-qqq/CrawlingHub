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

    private static final UserAgentErrorCode ERROR_CODE = UserAgentErrorCode.CIRCUIT_BREAKER_OPEN;

    public CircuitBreakerOpenException(double availableRate) {
        super(
                ERROR_CODE,
                String.format("UserAgent Pool Circuit Breaker OPEN (가용률: %.2f%%)", availableRate),
                Map.of("availableRate", availableRate));
    }

    public CircuitBreakerOpenException(String message) {
        super(ERROR_CODE, message);
    }
}
