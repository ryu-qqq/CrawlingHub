package com.ryuqq.crawlinghub.adapter.redis.pool;

import com.ryuqq.crawlinghub.application.token.port.UserAgentPoolPort;
import org.springframework.stereotype.Component;

/**
 * User-Agent Pool Adapter
 * UserAgentPoolManager를 Port 인터페이스로 wrapping
 *
 * @author crawlinghub
 */
@Component
public class UserAgentPoolAdapter implements UserAgentPoolPort {

    private final UserAgentPoolManager poolManager;

    public UserAgentPoolAdapter(UserAgentPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    @Override
    public void addToPool(Long userAgentId) {
        poolManager.addToPool(userAgentId);
    }

    @Override
    public Long acquireLeastRecentlyUsed() {
        return poolManager.acquireLeastRecentlyUsed();
    }

    @Override
    public void returnToPool(Long userAgentId) {
        poolManager.returnToPool(userAgentId);
    }

    @Override
    public void clearPool() {
        poolManager.clearPool();
    }
}
