package com.ryuqq.crawlinghub.integration.base;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.bootstrap.CrawlingHubApplication;
import com.ryuqq.crawlinghub.integration.config.DatabaseCleaner;
import com.ryuqq.crawlinghub.integration.config.TestContainersConfig;
import com.ryuqq.crawlinghub.integration.config.WireMockConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
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
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

/**
 * Base class for Web API integration tests.
 *
 * <p>Provides: - TestContainers (MySQL, Redis, LocalStack) - WireMock for external HTTP APIs -
 * TestRestTemplate for E2E HTTP testing - DatabaseCleaner for test isolation
 */
@SpringBootTest(
        classes = CrawlingHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import({
    TestContainersConfig.class,
    WireMockConfig.class,
    DatabaseCleaner.class,
    com.ryuqq.crawlinghub.integration.helper.TestDataHelper.class
})
public abstract class WebApiIntegrationTest {

    // Static containers for reuse across tests
    protected static final MySQLContainer MYSQL_CONTAINER;
    protected static final RedisContainer REDIS_CONTAINER;
    protected static final LocalStackContainer LOCALSTACK_CONTAINER;

    // SQS Queue URLs (required for outbox-based flows like retry)
    private static String crawlTaskQueueUrl;
    private static String productSyncQueueUrl;

    static {
        MYSQL_CONTAINER =
                new MySQLContainer(DockerImageName.parse("mysql:8.0"))
                        .withDatabaseName("crawlinghub_test")
                        .withUsername("test")
                        .withPassword("test")
                        .withReuse(true);

        REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:7.2")).withReuse(true);

        LOCALSTACK_CONTAINER =
                new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
                        .withServices("sqs")
                        .withReuse(true);

        MYSQL_CONTAINER.start();
        REDIS_CONTAINER.start();
        LOCALSTACK_CONTAINER.start();

        // Create SQS queues for publisher configuration
        SqsClient sqsClient =
                SqsClient.builder()
                        .endpointOverride(LOCALSTACK_CONTAINER.getEndpoint())
                        .region(Region.US_EAST_1)
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create("test", "test")))
                        .build();

        crawlTaskQueueUrl = createQueue(sqsClient, "crawl-task-queue-webapi-test");
        productSyncQueueUrl = createQueue(sqsClient, "product-sync-queue-webapi-test");
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

    @LocalServerPort protected int port;

    @Autowired protected TestRestTemplate restTemplate;

    @Autowired protected DatabaseCleaner databaseCleaner;

    @Autowired protected WireMockServer marketplaceWireMock;

    /** EventBridge Scheduler는 LocalStack에서 지원되지 않아 Mock 처리 */
    @MockBean protected EventBridgeClientPort eventBridgeClientPort;

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
                () -> LOCALSTACK_CONTAINER.getEndpoint().toString());
        registry.add("spring.cloud.aws.region.static", () -> "us-east-1");
        registry.add("spring.cloud.aws.credentials.access-key", () -> "test");
        registry.add("spring.cloud.aws.credentials.secret-key", () -> "test");

        // SQS Publisher Queue URLs (sqs-client module)
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
        resetWireMockServers();
    }

    private void resetWireMockServers() {
        if (marketplaceWireMock != null) {
            marketplaceWireMock.resetAll();
        }
    }

    /** Returns the base URL for API calls. */
    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    /** Returns the full URL for the given path. */
    protected String url(String path) {
        return baseUrl() + path;
    }

    /** Returns the API v1 base URL. */
    protected String apiV1Url(String path) {
        return url("/api/v1" + path);
    }
}
