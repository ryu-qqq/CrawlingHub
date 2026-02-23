package com.ryuqq.crawlinghub.application.useragent.port.in.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverLeakedUserAgentCommand;

/**
 * Leak된 UserAgent 복구 UseCase
 *
 * <p>BORROWED 상태로 지정 시간 이상 머문 UserAgent를 감지하여 강제 반납합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverLeakedUserAgentUseCase {

    /**
     * Leak된 UserAgent 감지 및 강제 반납
     *
     * @param command Leak 판정 기준 시간 커맨드
     * @return 강제 반납된 UserAgent 건수
     */
    int execute(RecoverLeakedUserAgentCommand command);
}
