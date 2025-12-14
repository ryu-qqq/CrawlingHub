package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.util.Map;

/**
 * 유효하지 않은 UserAgent 상태 전환 시 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public class InvalidUserAgentStateException extends DomainException {

    private static final UserAgentErrorCode ERROR_CODE =
            UserAgentErrorCode.INVALID_USER_AGENT_STATE;

    public InvalidUserAgentStateException(
            UserAgentStatus currentStatus, UserAgentStatus targetStatus) {
        super(
                ERROR_CODE,
                String.format("상태 전환 불가: %s → %s", currentStatus, targetStatus),
                Map.of(
                        "currentStatus", currentStatus.name(),
                        "targetStatus", targetStatus.name()));
    }

    public InvalidUserAgentStateException(String message) {
        super(ERROR_CODE, message);
    }
}
