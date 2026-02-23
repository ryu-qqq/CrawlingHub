package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverSuspendedPoolUserAgentUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SUSPENDED UserAgent 복구 서비스
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverSuspendedPoolUserAgentService implements RecoverSuspendedPoolUserAgentUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverSuspendedPoolUserAgentService.class);

    private final UserAgentPoolManager poolManager;

    public RecoverSuspendedPoolUserAgentService(UserAgentPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    @Override
    public int execute() {
        int recovered = poolManager.recoverSuspendedUserAgents();
        if (recovered > 0) {
            log.info("SUSPENDED -> SESSION_REQUIRED 복구: {}건", recovered);
        }
        return recovered;
    }
}
