package com.ryuqq.crawlinghub.domain.useragent.exception;

/**
 * 사용 가능한 UserAgent가 없을 때 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public class NoAvailableUserAgentException extends UserAgentException {

    private static final UserAgentErrorCode ERROR_CODE = UserAgentErrorCode.NO_AVAILABLE_USER_AGENT;

    public NoAvailableUserAgentException() {
        super(ERROR_CODE);
    }

    public NoAvailableUserAgentException(String message) {
        super(ERROR_CODE, message);
    }
}
