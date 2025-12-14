package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.util.Map;

/**
 * UserAgent를 찾을 수 없을 때 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public class UserAgentNotFoundException extends DomainException {

    private static final UserAgentErrorCode ERROR_CODE = UserAgentErrorCode.USER_AGENT_NOT_FOUND;

    public UserAgentNotFoundException(UserAgentId userAgentId) {
        super(
                ERROR_CODE,
                String.format("UserAgent를 찾을 수 없습니다: %d", userAgentId.value()),
                Map.of("userAgentId", userAgentId.value()));
    }

    public UserAgentNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
