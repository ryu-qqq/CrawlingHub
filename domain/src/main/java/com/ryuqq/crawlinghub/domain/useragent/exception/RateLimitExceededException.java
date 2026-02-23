package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.util.Map;

/**
 * Rate Limit 초과 시 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public class RateLimitExceededException extends UserAgentException {

    private static final UserAgentErrorCode ERROR_CODE = UserAgentErrorCode.RATE_LIMIT_EXCEEDED;

    public RateLimitExceededException(UserAgentId userAgentId) {
        super(
                ERROR_CODE,
                String.format("Rate Limit 초과: UserAgent %d", userAgentId.value()),
                Map.of("userAgentId", userAgentId.value()));
    }

    public RateLimitExceededException(String message) {
        super(ERROR_CODE, message);
    }
}
