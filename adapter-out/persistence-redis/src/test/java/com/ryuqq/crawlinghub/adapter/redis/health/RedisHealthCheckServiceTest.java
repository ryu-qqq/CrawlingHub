package com.ryuqq.crawlinghub.adapter.redis.health;

import com.redis.testcontainers.RedisContainer;
import com.ryuqq.crawlinghub.adapter.redis.TestRedisConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RedisHealthCheckService Integration Test
 *
 * @author crawlinghub
 */
@SpringBootTest(classes = TestRedisConfiguration.class)
@Testcontainers
class RedisHealthCheckServiceTest {

    @Container
    private static final RedisContainer REDIS_CONTAINER = new RedisContainer(
            DockerImageName.parse("redis:7.0-alpine")
    ).withExposedPorts(6379);

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Autowired
    private RedisHealthCheckService healthCheckService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        if (redisTemplate.getConnectionFactory() != null) {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        }
    }

    @Test
    void checkHealth_ShouldReturnHealthyStatus() {
        // when
        RedisHealthCheckService.HealthStatus status = healthCheckService.checkHealth();

        // then
        assertThat(status.isHealthy()).isTrue();
        assertThat(status.getMessage()).contains("healthy");
        assertThat(status.getResponseTimeMs()).isGreaterThanOrEqualTo(0);
        assertThat(status.getErrorDetails()).isNull();
    }

    @Test
    void executePing_ShouldReturnPong() {
        // when
        String result = healthCheckService.executePing();

        // then
        assertThat(result).isEqualTo("PONG");
    }

    @Test
    void testSetGet_ShouldReturnTrue() {
        // when
        boolean result = healthCheckService.testSetGet();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void getConnectionPoolStatus_ShouldReturnOperational() {
        // when
        RedisHealthCheckService.ConnectionPoolStatus status = healthCheckService.getConnectionPoolStatus();

        // then
        assertThat(status.isOperational()).isTrue();
        assertThat(status.getMessage()).contains("operational");
        assertThat(status.getErrorDetails()).isNull();
    }

    @Test
    void healthStatus_ToString_ShouldContainAllFields() {
        // given
        RedisHealthCheckService.HealthStatus status = new RedisHealthCheckService.HealthStatus(
                true, 10L, "test message", null
        );

        // when
        String result = status.toString();

        // then
        assertThat(result).contains("healthy=true");
        assertThat(result).contains("responseTimeMs=10");
        assertThat(result).contains("test message");
    }

    @Test
    void connectionPoolStatus_Getters_ShouldReturnCorrectValues() {
        // given
        RedisHealthCheckService.ConnectionPoolStatus status = new RedisHealthCheckService.ConnectionPoolStatus(
                true, "pool message", "error details"
        );

        // when & then
        assertThat(status.isOperational()).isTrue();
        assertThat(status.getMessage()).isEqualTo("pool message");
        assertThat(status.getErrorDetails()).isEqualTo("error details");
    }
}
