package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverUserAgentUseCase;
import org.springframework.stereotype.Service;

/**
 * UserAgent 복구 Service
 *
 * <p>{@link RecoverUserAgentUseCase} 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverUserAgentService implements RecoverUserAgentUseCase {

    private final UserAgentPoolManager poolManager;

    public RecoverUserAgentService(UserAgentPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    @Override
    public int recoverAll() {
        return poolManager.recoverSuspendedUserAgents();
    }
}
