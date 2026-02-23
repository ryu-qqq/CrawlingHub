package com.ryuqq.crawlinghub.application.useragent.manager;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.port.out.query.UserAgentPoolCacheQueryPort;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Cache Query Manager
 *
 * <p><strong>책임</strong>: Redis Pool 캐시 조회 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 QueryPort만 의존
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolCacheQueryManager {

    private final UserAgentPoolCacheQueryPort queryPort;

    public UserAgentPoolCacheQueryManager(UserAgentPoolCacheQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    public PoolStats getPoolStats() {
        return queryPort.getPoolStats();
    }

    public List<UserAgentId> getRecoverableUserAgents() {
        return queryPort.getRecoverableUserAgents();
    }

    public List<UserAgentId> getAllSuspendedUserAgents() {
        return queryPort.getAllSuspendedUserAgents();
    }

    public Optional<CachedUserAgent> findById(UserAgentId userAgentId) {
        return queryPort.findById(userAgentId);
    }

    public List<UserAgentId> getAllUserAgentIds() {
        return queryPort.getAllUserAgentIds();
    }

    public List<UserAgentId> getSessionExpiringUserAgents(int bufferMinutes) {
        return queryPort.getSessionExpiringUserAgents(bufferMinutes);
    }

    public List<UserAgentId> getSessionRequiredUserAgents() {
        return queryPort.getSessionRequiredUserAgents();
    }
}
