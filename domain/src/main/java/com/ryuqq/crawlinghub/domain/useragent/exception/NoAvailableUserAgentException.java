package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;

/**
 * 사용 가능한 UserAgent가 없을 때 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public class NoAvailableUserAgentException extends DomainException {

    public NoAvailableUserAgentException() {
        super(
                UserAgentErrorCode.NO_AVAILABLE_USER_AGENT.getCode(),
                UserAgentErrorCode.NO_AVAILABLE_USER_AGENT.getMessage());
    }

    public NoAvailableUserAgentException(String message) {
        super(UserAgentErrorCode.NO_AVAILABLE_USER_AGENT.getCode(), message);
    }
}
