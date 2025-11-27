package com.ryuqq.crawlinghub.application.useragent.service.query;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentPoolStatusUseCase;
import org.springframework.stereotype.Service;

/**
 * UserAgent Pool 상태 조회 Service
 *
 * <p>{@link GetUserAgentPoolStatusUseCase} 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetUserAgentPoolStatusService implements GetUserAgentPoolStatusUseCase {

    private final UserAgentPoolManager poolManager;

    public GetUserAgentPoolStatusService(UserAgentPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    @Override
    public UserAgentPoolStatusResponse execute() {
        PoolStats stats = poolManager.getPoolStats();

        UserAgentPoolStatusResponse.HealthScoreStats healthScoreStats =
                new UserAgentPoolStatusResponse.HealthScoreStats(
                        stats.avgHealthScore(), stats.minHealthScore(), stats.maxHealthScore());

        return UserAgentPoolStatusResponse.of(
                stats.total(), stats.available(), stats.suspended(), healthScoreStats);
    }
}
