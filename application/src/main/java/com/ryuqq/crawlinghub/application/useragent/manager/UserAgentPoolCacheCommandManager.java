package com.ryuqq.crawlinghub.application.useragent.manager;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPoolCacheCommandPort;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Cache Command Manager
 *
 * <p><strong>책임</strong>: Redis Pool 캐시 쓰기 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 CommandPort만 의존
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolCacheCommandManager {

    private final UserAgentPoolCacheCommandPort commandPort;

    public UserAgentPoolCacheCommandManager(UserAgentPoolCacheCommandPort commandPort) {
        this.commandPort = commandPort;
    }

    public Optional<CachedUserAgent> consumeToken() {
        return commandPort.consumeToken();
    }

    public void updateSession(
            UserAgentId userAgentId,
            String sessionToken,
            String nid,
            String mustitUid,
            Instant sessionExpiresAt) {
        commandPort.updateSession(userAgentId, sessionToken, nid, mustitUid, sessionExpiresAt);
    }

    public void expireSession(UserAgentId userAgentId) {
        commandPort.expireSession(userAgentId);
    }

    public void removeFromPool(UserAgentId userAgentId) {
        commandPort.removeFromPool(userAgentId);
    }

    public void suspendForRateLimit(UserAgentId userAgentId) {
        commandPort.suspendForRateLimit(userAgentId);
    }

    public void restoreToPool(UserAgentId userAgentId, String userAgentValue) {
        commandPort.restoreToPool(userAgentId, userAgentValue);
    }

    public int warmUp(List<CachedUserAgent> cachedUserAgents) {
        return commandPort.warmUp(cachedUserAgents);
    }

    public Optional<CachedUserAgent> borrow() {
        return commandPort.borrow();
    }

    public int returnAgent(
            long userAgentId,
            boolean success,
            int httpStatusCode,
            int healthDelta,
            Long cooldownUntil,
            int consecutiveRateLimits) {
        return commandPort.returnAgent(
                userAgentId,
                success,
                httpStatusCode,
                healthDelta,
                cooldownUntil,
                consecutiveRateLimits);
    }

    public int recoverExpiredCooldowns() {
        return commandPort.recoverExpiredCooldowns();
    }

    public List<Long> detectLeakedAgents(long leakThresholdMillis) {
        return commandPort.detectLeakedAgents(leakThresholdMillis);
    }

    public boolean isPoolInitialized() {
        return commandPort.isPoolInitialized();
    }

    public void markPoolInitialized() {
        commandPort.markPoolInitialized();
    }

    public boolean tryAcquireWarmUpLock() {
        return commandPort.tryAcquireWarmUpLock();
    }
}
