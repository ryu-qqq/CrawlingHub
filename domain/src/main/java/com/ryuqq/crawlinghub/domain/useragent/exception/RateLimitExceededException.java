package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.util.Map;

/**
 * Rate Limit 초과 시 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public class RateLimitExceededException extends DomainException {

    public RateLimitExceededException(UserAgentId userAgentId) {
        super(
                UserAgentErrorCode.RATE_LIMIT_EXCEEDED.getCode(),
                String.format("Rate Limit 초과: UserAgent %d", userAgentId.value()),
                Map.of("userAgentId", userAgentId.value()));
    }

    public RateLimitExceededException(String message) {
        super(UserAgentErrorCode.RATE_LIMIT_EXCEEDED.getCode(), message);
    }
}
