package com.ryuqq.crawlinghub.integration.base;

import com.redis.testcontainers.RedisContainer;
import com.ryuqq.crawlinghub.SchedulerApplication;
import com.ryuqq.crawlinghub.integration.config.DatabaseCleaner;
import com.ryuqq.crawlinghub.integration.config.TestContainersConfig;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

/**
 * Base class for Scheduler integration tests.
 *
 * <p>Provides: - TestContainers (MySQL, Redis, LocalStack) - Mock SchedulerClient (EventBridge
 * Scheduler is not supported by LocalStack) - Real SQS queues for outbox publishing -
 * DatabaseCleaner for test isolation
 */
@SpringBootTest(classes = SchedulerApplication.class)
@ActiveProfiles("test")
@Testcontainers
@Import({
    TestContainersConfig.class,
    DatabaseCleaner.class,
    com.ryuqq.crawlinghub.integration.helper.TestDataHelper.class
})
public abstract class SchedulerIntegrationTest {

    // Static containers for reuse across tests
    protected static final MySQLContainer MYSQL_CONTAINER;
    protected static final RedisContainer REDIS_CONTAINER;
    protected static final LocalStackContainer LOCALSTACK_CONTAINER;

    // SQS Queue URLs (required for outbox-based publishing)
    private static String crawlTaskQueueUrl;
    private static String productSyncQueueUrl;

    static {
        // CI 환경(JVM=UTC)에서도 MySQL(Asia/Seoul)과 일치하도록 JVM 타임존 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));

        MYSQL_CONTAINER =
                new MySQLContainer(DockerImageName.parse("mysql:8.0"))
                        .withDatabaseName("crawlinghub_test")
                        .withUsername("test")
                        .withPassword("test")
                        .withEnv("TZ", "Asia/Seoul")
                        .withCommand("--default-time-zone=+09:00")
                        .withReuse(true);

        REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:7.2")).withReuse(true);

        LOCALSTACK_CONTAINER =
                new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
                        .withServices("sqs")
                        .withReuse(true);

        MYSQL_CONTAINER.start();
        REDIS_CONTAINER.start();
        LOCALSTACK_CONTAINER.start();

        // Create SQS queues for outbox publishing
        SqsClient sqsClient =
                SqsClient.builder()
                        .endpointOverride(LOCALSTACK_CONTAINER.getEndpoint())
                        .region(Region.US_EAST_1)
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create("test", "test")))
                        .build();

        crawlTaskQueueUrl = createQueue(sqsClient, "crawl-task-queue-scheduler-test");
        productSyncQueueUrl = createQueue(sqsClient, "product-sync-queue-scheduler-test");
    }

    private static String createQueue(SqsClient sqsClient, String queueName) {
        try {
            return sqsClient
                    .createQueue(CreateQueueRequest.builder().queueName(queueName).build())
                    .queueUrl();
        } catch (Exception e) {
            return sqsClient.getQueueUrl(r -> r.queueName(queueName)).queueUrl();
        }
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

        // LocalStack SQS (Spring Cloud AWS for listeners)
        registry.add(
                "spring.cloud.aws.sqs.endpoint",
                () -> LOCALSTACK_CONTAINER.getEndpoint().toString());
        registry.add("spring.cloud.aws.region.static", () -> "us-east-1");
        registry.add("spring.cloud.aws.credentials.access-key", () -> "test");
        registry.add("spring.cloud.aws.credentials.secret-key", () -> "test");

        // SQS Listener Queue URLs (required for bean creation even though disabled)
        registry.add("aws.sqs.listener.crawl-task-queue-url", () -> crawlTaskQueueUrl);
        registry.add("aws.sqs.listener.crawl-task-dlq-url", () -> crawlTaskQueueUrl);
        registry.add("aws.sqs.listener.event-bridge-trigger-queue-url", () -> crawlTaskQueueUrl);
        registry.add("aws.sqs.listener.product-sync-queue-url", () -> productSyncQueueUrl);

        // Disable all SQS listeners (scheduler tests don't need SQS consumers)
        registry.add("aws.sqs.listener.crawl-task-listener-enabled", () -> "false");
        registry.add("aws.sqs.listener.event-bridge-trigger-listener-enabled", () -> "false");
        registry.add("aws.sqs.listener.product-sync-listener-enabled", () -> "false");
        registry.add("aws.sqs.listener.crawl-task-dlq-listener-enabled", () -> "false");

        // SQS Publisher Queue URLs (real LocalStack queues for outbox publishing)
        registry.add("sqs.endpoint", () -> LOCALSTACK_CONTAINER.getEndpoint().toString());
        registry.add("sqs.region", () -> "us-east-1");
        registry.add("sqs.queues.crawl-task", () -> crawlTaskQueueUrl);
        registry.add("sqs.queues.product-sync", () -> productSyncQueueUrl);

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
