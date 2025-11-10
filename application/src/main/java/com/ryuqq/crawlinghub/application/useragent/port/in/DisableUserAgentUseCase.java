package com.ryuqq.crawlinghub.application.useragent.port.in;

import com.ryuqq.crawlinghub.application.useragent.dto.command.DisableUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;

/**
 * UserAgent 비활성화 UseCase
 *
 * <p>UserAgent를 비활성화합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface DisableUserAgentUseCase {

    /**
     * UserAgent 비활성화
     *
     * @param command UserAgent 비활성화 Command
     * @return 비활성화된 UserAgent 정보
     */
    UserAgentResponse execute(DisableUserAgentCommand command);
}



