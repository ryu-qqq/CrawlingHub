package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import java.util.Map;

/**
 * UserAgent BC 예외의 베이스 클래스
 *
 * <p>UserAgent 바운디드 컨텍스트의 모든 예외는 이 클래스를 상속해야 합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public abstract class UserAgentException extends DomainException {

    protected UserAgentException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected UserAgentException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected UserAgentException(ErrorCode errorCode, String message, Map<String, Object> args) {
        super(errorCode, message, args);
    }

    protected UserAgentException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
