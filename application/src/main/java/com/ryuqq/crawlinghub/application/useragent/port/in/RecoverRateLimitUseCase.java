package com.ryuqq.crawlinghub.application.useragent.port.in;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverRateLimitCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;

/**
 * Rate Limit 복구 UseCase
 *
 * <p>UserAgent의 Rate Limit을 복구합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface RecoverRateLimitUseCase {

    /**
     * Rate Limit 복구
     *
     * @param command Rate Limit 복구 Command
     * @return 복구된 UserAgent 정보
     */
    UserAgentResponse execute(RecoverRateLimitCommand command);
}



