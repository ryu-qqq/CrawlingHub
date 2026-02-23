package com.ryuqq.crawlinghub.application.useragent.manager;

import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.SessionTokenPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Session Token Manager
 *
 * <p><strong>책임</strong>: 세션 토큰 발급 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 SessionTokenPort만 의존
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SessionTokenManager {

    private final SessionTokenPort sessionTokenPort;

    public SessionTokenManager(SessionTokenPort sessionTokenPort) {
        this.sessionTokenPort = sessionTokenPort;
    }

    /**
     * 세션 토큰 발급
     *
     * @param userAgentValue User-Agent 헤더 값
     * @return 발급된 세션 토큰 (실패 시 empty)
     */
    public Optional<SessionToken> issueSessionToken(String userAgentValue) {
        return sessionTokenPort.issueSessionToken(userAgentValue);
    }
}
