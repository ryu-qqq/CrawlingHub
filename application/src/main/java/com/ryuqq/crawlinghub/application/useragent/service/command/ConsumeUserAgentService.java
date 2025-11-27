package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.ConsumeUserAgentUseCase;
import org.springframework.stereotype.Service;

/**
 * UserAgent 토큰 소비 Service
 *
 * <p>{@link ConsumeUserAgentUseCase} 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ConsumeUserAgentService implements ConsumeUserAgentUseCase {

    private final UserAgentPoolManager poolManager;

    public ConsumeUserAgentService(UserAgentPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    @Override
    public CachedUserAgent execute() {
        return poolManager.consume();
    }
}
