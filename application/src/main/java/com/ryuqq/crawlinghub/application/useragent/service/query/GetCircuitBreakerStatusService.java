package com.ryuqq.crawlinghub.application.useragent.service.query;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.dto.response.CircuitBreakerStatusResponse;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetCircuitBreakerStatusUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Circuit Breaker 상태 조회 Service
 *
 * <p>{@link GetCircuitBreakerStatusUseCase} 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetCircuitBreakerStatusService implements GetCircuitBreakerStatusUseCase {

    private final UserAgentPoolCacheManager cacheManager;
    private final double circuitBreakerThreshold;

    public GetCircuitBreakerStatusService(
            UserAgentPoolCacheManager cacheManager,
            @Value("${useragent.circuit-breaker.threshold:20.0}") double circuitBreakerThreshold) {
        this.cacheManager = cacheManager;
        this.circuitBreakerThreshold = circuitBreakerThreshold;
    }

    @Override
    public CircuitBreakerStatusResponse execute() {
        PoolStats stats = cacheManager.getPoolStats();

        return CircuitBreakerStatusResponse.of(
                stats.availableRate(),
                circuitBreakerThreshold,
                stats.total(),
                stats.available(),
                stats.suspended());
    }
}
