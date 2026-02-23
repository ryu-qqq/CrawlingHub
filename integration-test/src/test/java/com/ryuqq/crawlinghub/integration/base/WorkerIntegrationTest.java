package com.ryuqq.crawlinghub.integration.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import com.ryuqq.crawlinghub.integration.config.DatabaseCleaner;
import com.ryuqq.crawlinghub.integration.config.TestClockConfig;
import com.ryuqq.crawlinghub.integration.config.TestContainersConfig;
import com.ryuqq.crawlinghub.integration.config.TestCrawlWorkerApplication;
import com.ryuqq.crawlinghub.integration.config.WireMockConfig;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Base class for Crawl Worker integration tests.
 *
 * <p>Provides:
 *
 * <ul>
 *   <li>TestContainers (MySQL, Redis, LocalStack with SQS)
 *   <li>WireMock for crawl targets and external APIs
 *   <li>SqsClient for queue manipulation
 *   <li>DatabaseCleaner for test isolation
 *   <li>Awaitility for async testing
 * </ul>
 *
 * <p>SQS Queue URLs are created in static block and injected via DynamicPropertySource to
 * ensure @SqsListener can receive messages properly.
 */
@SpringBootTest(classes = TestCrawlWorkerApplication.class)
@ActiveProfiles("test")
@Testcontainers
@Import({TestContainersConfig.class, WireMockConfig.class, TestClockConfig.class})
public abstract class WorkerIntegrationTest {

    // Queue names
    protected static final String CRAWL_TASK_QUEUE_NAME = "crawl-task-queue-test";
    protected static final String CRAWL_TASK_DLQ_NAME = "crawl-task-dlq-test";
    protected static final String EVENTBRIDGE_TRIGGER_QUEUE_NAME = "eventbridge-trigger-queue-test";
    protected static final String PRODUCT_SYNC_QUEUE_NAME = "product-sync-queue-test";

    // Static containers for reuse across tests
    protected static final MySQLContainer MYSQL_CONTAINER;
    protected static final RedisContainer REDIS_CONTAINER;
    protected static final LocalStackContainer LOCALSTACK_CONTAINER;
    protected static SqsClient sqsClient;

    // Queue URLs (set after queue creation)
    protected static String crawlTaskQueueUrl;
    protected static String crawlTaskDlqUrl;
    protected static String eventBridgeTriggerQueueUrl;
    protected static String productSyncQueueUrl;

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

        // Initialize SQS client
        sqsClient =
                SqsClient.builder()
                        .endpointOverride(LOCALSTACK_CONTAINER.getEndpoint())
                        .region(Region.US_EAST_1)
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create("test", "test")))
                        .build();

        // Create SQS queues for @SqsListener
        crawlTaskQueueUrl = createQueueIfNotExists(CRAWL_TASK_QUEUE_NAME);
        crawlTaskDlqUrl = createQueueIfNotExists(CRAWL_TASK_DLQ_NAME);
        eventBridgeTriggerQueueUrl = createQueueIfNotExists(EVENTBRIDGE_TRIGGER_QUEUE_NAME);
        productSyncQueueUrl = createQueueIfNotExists(PRODUCT_SYNC_QUEUE_NAME);
    }

    private static String createQueueIfNotExists(String queueName) {
        try {
            return sqsClient
                    .createQueue(CreateQueueRequest.builder().queueName(queueName).build())
                    .queueUrl();
        } catch (Exception e) {
            // Queue might already exist from previous test run
            return sqsClient.getQueueUrl(r -> r.queueName(queueName)).queueUrl();
        }
    }

    @Autowired protected DatabaseCleaner databaseCleaner;

    @Autowired protected WireMockServer crawlTargetWireMock;

    @Autowired protected WireMockServer marketplaceWireMock;

    @Autowired protected ObjectMapper objectMapper;

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

        // SQS Queue URLs for @SqsListener
        registry.add("aws.sqs.listener.crawl-task-queue-url", () -> crawlTaskQueueUrl);
        registry.add("aws.sqs.listener.crawl-task-dlq-url", () -> crawlTaskDlqUrl);
        registry.add(
                "aws.sqs.listener.event-bridge-trigger-queue-url",
                () -> eventBridgeTriggerQueueUrl);
        // SQS Publisher Queue URLs (sqs-client module)
        registry.add("sqs.endpoint", () -> LOCALSTACK_CONTAINER.getEndpoint().toString());
        registry.add("sqs.region", () -> "us-east-1");
        registry.add("sqs.queues.crawl-task", () -> crawlTaskQueueUrl);
        registry.add("sqs.queues.product-sync", () -> productSyncQueueUrl);

        // SQS Listener ProductSync Queue URL
        registry.add("aws.sqs.listener.product-sync-queue-url", () -> productSyncQueueUrl);

        // SQS Listener enabled for testing
        registry.add("aws.sqs.listener.crawl-task-listener-enabled", () -> "true");
        registry.add("aws.sqs.listener.event-bridge-trigger-listener-enabled", () -> "false");
        registry.add("aws.sqs.listener.product-sync-listener-enabled", () -> "false");

        // DLQ Listeners enabled for testing (disabled - DLQ testing is optional)
        registry.add("aws.sqs.listener.crawl-task-dlq-listener-enabled", () -> "false");

        // Flyway
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");

        // JPA
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @BeforeAll
    static void setupAwaitility() {
        Awaitility.setDefaultTimeout(Duration.ofSeconds(10));
        Awaitility.setDefaultPollInterval(Duration.ofMillis(500));
    }

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        resetWireMockServers();
        purgeQueues();
    }

    private void resetWireMockServers() {
        if (crawlTargetWireMock != null) {
            crawlTargetWireMock.resetAll();
        }
        if (marketplaceWireMock != null) {
            marketplaceWireMock.resetAll();
        }
    }

    private void purgeQueues() {
        try {
            sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(crawlTaskQueueUrl).build());
        } catch (Exception e) {
            // Ignore purge errors (queue might be empty or rate limited)
        }
    }

    /**
     * Creates an SQS queue for testing.
     *
     * @param queueName the name of the queue
     * @return the queue URL
     */
    protected String createQueue(String queueName) {
        return sqsClient
                .createQueue(CreateQueueRequest.builder().queueName(queueName).build())
                .queueUrl();
    }

    /**
     * Sends a message to the specified queue.
     *
     * @param queueUrl the URL of the queue
     * @param messageBody the message body (JSON string)
     */
    protected void sendSqsMessage(String queueUrl, String messageBody) {
        sqsClient.sendMessage(
                SendMessageRequest.builder().queueUrl(queueUrl).messageBody(messageBody).build());
    }

    /**
     * Sends a CrawlTask message to the crawl task queue.
     *
     * @param taskId the task ID
     * @param schedulerId the scheduler ID
     * @param sellerId the seller ID
     * @param taskType the task type (e.g., "SEARCH", "DETAIL")
     * @param endpoint the crawl endpoint URL
     */
    protected void sendCrawlTaskMessage(
            Long taskId, Long schedulerId, Long sellerId, String taskType, String endpoint) {
        String messageBody =
                String.format(
                        "{\"taskId\":%d,\"schedulerId\":%d,\"sellerId\":%d,\"taskType\":\"%s\",\"endpoint\":\"%s\"}",
                        taskId, schedulerId, sellerId, taskType, endpoint);
        sendSqsMessage(crawlTaskQueueUrl, messageBody);
    }

    /**
     * Gets the crawl task queue URL.
     *
     * @return the queue URL
     */
    protected String getCrawlTaskQueueUrl() {
        return crawlTaskQueueUrl;
    }

    /**
     * Gets the SQS client for direct queue operations.
     *
     * @return the SQS client
     */
    protected SqsClient getSqsClient() {
        return sqsClient;
    }
}
