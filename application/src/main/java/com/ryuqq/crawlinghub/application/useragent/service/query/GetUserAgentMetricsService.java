package com.ryuqq.crawlinghub.application.useragent.service.query;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.dto.response.CircuitBreakerStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentMetricsResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentMetricsResponse.HealthScoreDistribution;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentMetricsUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * UserAgent Metrics 조회 Service
 *
 * <p>{@link GetUserAgentMetricsUseCase} 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetUserAgentMetricsService implements GetUserAgentMetricsUseCase {

    private final UserAgentPoolCacheManager cacheManager;
    private final double circuitBreakerThreshold;

    public GetUserAgentMetricsService(
            UserAgentPoolCacheManager cacheManager,
            @Value("${useragent.circuit-breaker.threshold:20.0}") double circuitBreakerThreshold) {
        this.cacheManager = cacheManager;
        this.circuitBreakerThreshold = circuitBreakerThreshold;
    }

    @Override
    public UserAgentMetricsResponse execute() {
        PoolStats cacheStats = cacheManager.getPoolStats();

        // Pool 통계 생성
        UserAgentMetricsResponse.PoolStats poolStats =
                UserAgentMetricsResponse.PoolStats.of(
                        cacheStats.total(),
                        cacheStats.available(),
                        cacheStats.suspended(),
                        0); // blocked는 Redis Pool에서 관리하지 않음

        // Health Score 분포 생성 (Redis에서 조회된 통계 사용)
        HealthScoreDistribution healthDist =
                HealthScoreDistribution.of(
                        cacheStats.avgHealthScore(),
                        cacheStats.minHealthScore(),
                        cacheStats.maxHealthScore(),
                        0, // healthyCount - 추후 구현
                        0, // warningCount - 추후 구현
                        0); // criticalCount - 추후 구현

        // Circuit Breaker 상태 생성
        CircuitBreakerStatusResponse cbStatus =
                CircuitBreakerStatusResponse.of(
                        cacheStats.availableRate(),
                        circuitBreakerThreshold,
                        cacheStats.total(),
                        cacheStats.available(),
                        cacheStats.suspended());

        return UserAgentMetricsResponse.of(poolStats, healthDist, cbStatus);
    }
}
