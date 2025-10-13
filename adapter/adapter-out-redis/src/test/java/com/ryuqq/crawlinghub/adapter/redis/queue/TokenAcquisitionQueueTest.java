package com.ryuqq.crawlinghub.adapter.redis.queue;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenAcquisitionQueue Integration Test
 *
 * @author crawlinghub
 */
@SpringBootTest(classes = TestRedisConfiguration.class)
@Testcontainers
class TokenAcquisitionQueueTest {

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
    private TokenAcquisitionQueue queue;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long USER_AGENT_ID = 1L;

    @BeforeEach
    void setUp() {
        if (redisTemplate.getConnectionFactory() != null) {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        }
    }

    @Test
    void enqueue_ShouldAddRequestToQueue() {
        // given
        String requestId = "request-1";

        // when
        queue.enqueue(USER_AGENT_ID, requestId);

        // then
        Long queueSize = queue.getQueueSize(USER_AGENT_ID);
        assertThat(queueSize).isEqualTo(1);
    }

    @Test
    void dequeue_ShouldReturnRequestInFIFOOrder() {
        // given
        queue.enqueue(USER_AGENT_ID, "request-1");
        queue.enqueue(USER_AGENT_ID, "request-2");
        queue.enqueue(USER_AGENT_ID, "request-3");

        // when
        String first = queue.dequeue(USER_AGENT_ID);
        String second = queue.dequeue(USER_AGENT_ID);
        String third = queue.dequeue(USER_AGENT_ID);

        // then
        assertThat(first).isEqualTo("request-1");
        assertThat(second).isEqualTo("request-2");
        assertThat(third).isEqualTo("request-3");
    }

    @Test
    void dequeue_OnEmptyQueue_ShouldReturnNull() {
        // when
        String result = queue.dequeue(USER_AGENT_ID);

        // then
        assertThat(result).isNull();
    }

    @Test
    void getQueueSize_ShouldReturnCorrectSize() {
        // given
        queue.enqueue(USER_AGENT_ID, "request-1");
        queue.enqueue(USER_AGENT_ID, "request-2");

        // when
        Long size = queue.getQueueSize(USER_AGENT_ID);

        // then
        assertThat(size).isEqualTo(2);
    }

    @Test
    void clearQueue_ShouldRemoveAllRequests() {
        // given
        queue.enqueue(USER_AGENT_ID, "request-1");
        queue.enqueue(USER_AGENT_ID, "request-2");

        // when
        queue.clearQueue(USER_AGENT_ID);

        // then
        Long size = queue.getQueueSize(USER_AGENT_ID);
        assertThat(size).isEqualTo(0);
    }

    @Test
    void getAllPendingRequests_ShouldReturnAllRequestsInOrder() {
        // given
        queue.enqueue(USER_AGENT_ID, "request-1");
        queue.enqueue(USER_AGENT_ID, "request-2");
        queue.enqueue(USER_AGENT_ID, "request-3");

        // when
        List<Object> pendingRequests = queue.getAllPendingRequests(USER_AGENT_ID);

        // then
        assertThat(pendingRequests).hasSize(3);
        assertThat(pendingRequests.get(0)).isEqualTo("request-3");
        assertThat(pendingRequests.get(1)).isEqualTo("request-2");
        assertThat(pendingRequests.get(2)).isEqualTo("request-1");
    }

    @Test
    void multipleUserAgents_ShouldMaintainSeparateQueues() {
        // given
        Long userAgent1 = 1L;
        Long userAgent2 = 2L;

        // when
        queue.enqueue(userAgent1, "request-1-1");
        queue.enqueue(userAgent2, "request-2-1");
        queue.enqueue(userAgent1, "request-1-2");

        // then
        assertThat(queue.getQueueSize(userAgent1)).isEqualTo(2);
        assertThat(queue.getQueueSize(userAgent2)).isEqualTo(1);
        assertThat(queue.dequeue(userAgent1)).isEqualTo("request-1-1");
        assertThat(queue.dequeue(userAgent2)).isEqualTo("request-2-1");
    }
}
