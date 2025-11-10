package com.ryuqq.crawlinghub.adapter.redis;

import com.ryuqq.crawlinghub.adapter.redis.circuit.CircuitBreakerManager;
import com.ryuqq.crawlinghub.adapter.redis.config.RedisConfig;
import com.ryuqq.crawlinghub.adapter.redis.health.RedisHealthCheckService;
import com.ryuqq.crawlinghub.adapter.redis.lock.DistributedLockService;
import com.ryuqq.crawlinghub.adapter.redis.pool.UserAgentPoolManager;
import com.ryuqq.crawlinghub.adapter.redis.queue.TokenAcquisitionQueue;
import com.ryuqq.crawlinghub.adapter.redis.ratelimit.TokenBucketRateLimiter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Test Configuration for Redis Tests
 *
 * @author crawlinghub
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.ryuqq.crawlinghub.adapter.redis")
@Import({
    RedisConfig.class,
    TokenBucketRateLimiter.class,
    CircuitBreakerManager.class,
    UserAgentPoolManager.class,
    DistributedLockService.class,
    TokenAcquisitionQueue.class,
    RedisHealthCheckService.class
})
public class TestRedisConfiguration {
}
