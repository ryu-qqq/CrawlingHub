package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;

/**
 * 세션 토큰 발급 실패 시 발생하는 예외
 *
 * <p>Lazy Token Issuance 전략에서 세션 발급이 실패했을 때 발생합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public class SessionIssuanceFailedException extends DomainException {

    private static final UserAgentErrorCode ERROR_CODE = UserAgentErrorCode.SESSION_ISSUANCE_FAILED;

    private final Long userAgentId;

    public SessionIssuanceFailedException(Long userAgentId, String reason) {
        super(ERROR_CODE, String.format("UserAgent %d 세션 발급 실패: %s", userAgentId, reason));
        this.userAgentId = userAgentId;
    }

    public Long getUserAgentId() {
        return userAgentId;
    }
}
