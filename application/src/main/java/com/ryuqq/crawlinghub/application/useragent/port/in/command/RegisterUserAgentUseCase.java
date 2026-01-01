package com.ryuqq.crawlinghub.application.useragent.port.in.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RegisterUserAgentCommand;

/**
 * UserAgent 등록 UseCase
 *
 * <p>새로운 UserAgent를 등록하는 UseCase입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RegisterUserAgentUseCase {

    /**
     * UserAgent 등록
     *
     * @param command 등록 Command
     * @return 등록된 UserAgent ID
     */
    Long register(RegisterUserAgentCommand command);
}
