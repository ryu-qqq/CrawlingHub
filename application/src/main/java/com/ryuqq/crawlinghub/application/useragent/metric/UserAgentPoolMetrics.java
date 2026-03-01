package com.ryuqq.crawlinghub.application.useragent.metric;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheQueryManager;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserAgentPoolMetrics {

    private static final Logger log = LoggerFactory.getLogger(UserAgentPoolMetrics.class);
    private static final String PREFIX = "crawlinghub.";
    private static final long REFRESH_INTERVAL_MS = 15_000;

    private final UserAgentPoolCacheQueryManager queryManager;

    private volatile PoolStats cachedStats = PoolStats.empty();
    private volatile long lastRefreshTime = 0;

    public UserAgentPoolMetrics(
            MeterRegistry meterRegistry, UserAgentPoolCacheQueryManager queryManager) {
        this.queryManager = queryManager;

        Gauge.builder(PREFIX + "useragent_pool_total", this, m -> m.getStats().total())
                .description("Pool 전체 UserAgent 수")
                .register(meterRegistry);

        Gauge.builder(PREFIX + "useragent_pool_available", this, m -> m.getStats().available())
                .description("가용 상태 UserAgent 수")
                .register(meterRegistry);

        Gauge.builder(PREFIX + "useragent_pool_borrowed", this, m -> m.getStats().borrowed())
                .description("현재 크롤링 중 UserAgent 수")
                .register(meterRegistry);

        Gauge.builder(PREFIX + "useragent_pool_cooldown", this, m -> m.getStats().cooldown())
                .description("쿨다운 대기 중 UserAgent 수")
                .register(meterRegistry);

        Gauge.builder(PREFIX + "useragent_pool_suspended", this, m -> m.getStats().suspended())
                .description("일시정지 상태 UserAgent 수")
                .register(meterRegistry);

        Gauge.builder(
                        PREFIX + "useragent_pool_health_avg",
                        this,
                        m -> m.getStats().avgHealthScore())
                .description("평균 건강도 점수")
                .register(meterRegistry);

        Gauge.builder(
                        PREFIX + "useragent_pool_available_rate",
                        this,
                        m -> m.getStats().availableRate())
                .description("가용률 (%)")
                .register(meterRegistry);
    }

    private PoolStats getStats() {
        long now = System.currentTimeMillis();
        if (now - lastRefreshTime >= REFRESH_INTERVAL_MS) {
            try {
                cachedStats = queryManager.getPoolStats();
            } catch (Exception e) {
                log.warn("UserAgent Pool 통계 조회 실패, 캐시된 값 사용", e);
            }
            lastRefreshTime = now;
        }
        return cachedStats;
    }
}
