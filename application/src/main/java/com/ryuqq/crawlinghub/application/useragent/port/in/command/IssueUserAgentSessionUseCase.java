package com.ryuqq.crawlinghub.application.useragent.port.in.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand;

/**
 * UserAgent 세션 발급 UseCase
 *
 * <p>세션 만료 임박 갱신(RENEW) 또는 신규 세션 발급(NEW)을 수행합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface IssueUserAgentSessionUseCase {

    /**
     * 세션 발급 실행
     *
     * @param command 세션 발급 커맨드 (타입, 배치 크기, 딜레이 등)
     * @return 성공한 세션 발급 건수
     */
    int execute(IssueUserAgentSessionCommand command);
}
