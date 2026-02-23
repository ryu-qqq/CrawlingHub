package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverCooldownUserAgentUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * COOLDOWN 만료 UserAgent 복구 서비스
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverCooldownUserAgentService implements RecoverCooldownUserAgentUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverCooldownUserAgentService.class);

    private final UserAgentPoolCacheCommandManager cacheCommandManager;

    public RecoverCooldownUserAgentService(UserAgentPoolCacheCommandManager cacheCommandManager) {
        this.cacheCommandManager = cacheCommandManager;
    }

    @Override
    public int execute() {
        int recovered = cacheCommandManager.recoverExpiredCooldowns();
        if (recovered > 0) {
            log.info("COOLDOWN -> IDLE/SESSION_REQUIRED 복구: {}건", recovered);
        }
        return recovered;
    }
}
