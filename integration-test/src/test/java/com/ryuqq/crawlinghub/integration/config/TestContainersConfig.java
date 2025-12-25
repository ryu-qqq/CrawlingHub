package com.ryuqq.crawlinghub.integration.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainers configuration for integration tests. Provides MySQL, Redis, and LocalStack (SQS)
 * containers.
 *
 * <p>All containers are configured with reuse enabled for faster test execution.
 */
@TestConfiguration
public class TestContainersConfig {

    private static final String MYSQL_IMAGE = "mysql:8.0";
    private static final String REDIS_IMAGE = "redis:7.2";
    private static final String LOCALSTACK_IMAGE = "localstack/localstack:3.0";

    @Bean(initMethod = "start", destroyMethod = "stop")
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
                .withDatabaseName("crawlinghub_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse(REDIS_IMAGE)).withReuse(true);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public LocalStackContainer localStackContainer() {
        return new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE))
                .withServices(LocalStackContainer.Service.SQS)
                .withReuse(true);
    }
}
