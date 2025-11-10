package com.ryuqq.crawlinghub.application.useragent.port.in;

import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueTokenCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;

/**
 * 토큰 발급 UseCase
 *
 * <p>UserAgent에 새 토큰을 발급합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface IssueTokenUseCase {

    /**
     * 토큰 발급
     *
     * @param command 토큰 발급 Command
     * @return 토큰이 발급된 UserAgent 정보
     */
    UserAgentResponse execute(IssueTokenCommand command);
}



