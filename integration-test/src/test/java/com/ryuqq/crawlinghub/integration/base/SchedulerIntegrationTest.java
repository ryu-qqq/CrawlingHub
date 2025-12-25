package com.ryuqq.crawlinghub.integration.base;

import com.redis.testcontainers.RedisContainer;
import com.ryuqq.crawlinghub.SchedulerApplication;
import com.ryuqq.crawlinghub.integration.config.DatabaseCleaner;
import com.ryuqq.crawlinghub.integration.config.TestContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.scheduler.SchedulerClient;

/**
 * Base class for Scheduler integration tests.
 *
 * <p>Provides: - TestContainers (MySQL, Redis, LocalStack) - Mock SchedulerClient (EventBridge
 * Scheduler is not supported by LocalStack) - DatabaseCleaner for test isolation
 */
@SpringBootTest(classes = SchedulerApplication.class)
@ActiveProfiles("test")
@Testcontainers
@Import(TestContainersConfig.class)
public abstract class SchedulerIntegrationTest {

    // Static containers for reuse across tests
    protected static final MySQLContainer<?> MYSQL_CONTAINER;
    protected static final RedisContainer REDIS_CONTAINER;
    protected static final LocalStackContainer LOCALSTACK_CONTAINER;

    static {
        MYSQL_CONTAINER =
                new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                        .withDatabaseName("crawlinghub_test")
                        .withUsername("test")
                        .withPassword("test")
                        .withReuse(true);

        REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:7.2")).withReuse(true);

        LOCALSTACK_CONTAINER =
                new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
                        .withServices(LocalStackContainer.Service.SQS)
                        .withReuse(true);

        MYSQL_CONTAINER.start();
        REDIS_CONTAINER.start();
        LOCALSTACK_CONTAINER.start();
    }

    @Autowired protected DatabaseCleaner databaseCleaner;

    // EventBridge Scheduler is not supported by LocalStack, so we mock it
    @MockBean protected SchedulerClient schedulerClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // Redis
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);

        // LocalStack SQS
        registry.add(
                "spring.cloud.aws.sqs.endpoint",
                () ->
                        LOCALSTACK_CONTAINER
                                .getEndpointOverride(LocalStackContainer.Service.SQS)
                                .toString());
        registry.add("spring.cloud.aws.region.static", () -> "us-east-1");
        registry.add("spring.cloud.aws.credentials.access-key", () -> "test");
        registry.add("spring.cloud.aws.credentials.secret-key", () -> "test");

        // Flyway
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");

        // JPA
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }
}
