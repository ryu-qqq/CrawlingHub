package com.ryuqq.crawlinghub.adapter.redis.pool;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("UserAgentPoolManager 테스트")
class UserAgentPoolManagerTest {

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private UserAgentPoolManager poolManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        poolManager.clearPool();
    }

    @Test
    @DisplayName("Pool에 User-Agent를 추가할 수 있다")
    void canAddUserAgentToPool() {
        // Given
        Long userAgentId = 1L;

        // When
        poolManager.addToPool(userAgentId);

        // Then
        Long poolSize = poolManager.getPoolSize();
        assertThat(poolSize).isEqualTo(1);
    }

    @Test
    @DisplayName("LRU 방식으로 User-Agent를 획득한다")
    void acquiresLeastRecentlyUsed() throws InterruptedException {
        // Given
        poolManager.addToPool(1L);
        Thread.sleep(10);
        poolManager.addToPool(2L);
        Thread.sleep(10);
        poolManager.addToPool(3L);

        // When
        Long acquired = poolManager.acquireLeastRecentlyUsed();

        // Then
        assertThat(acquired).isEqualTo(1L); // 가장 먼저 추가된 것
        assertThat(poolManager.getPoolSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("User-Agent를 Pool에 반환할 수 있다")
    void canReturnUserAgentToPool() {
        // Given
        Long userAgentId = 1L;
        poolManager.addToPool(userAgentId);
        poolManager.acquireLeastRecentlyUsed();

        // When
        poolManager.returnToPool(userAgentId);

        // Then
        Long poolSize = poolManager.getPoolSize();
        assertThat(poolSize).isEqualTo(1);
    }

    @Test
    @DisplayName("Pool에서 User-Agent를 제거할 수 있다")
    void canRemoveUserAgentFromPool() {
        // Given
        Long userAgentId = 1L;
        poolManager.addToPool(userAgentId);

        // When
        poolManager.removeFromPool(userAgentId);

        // Then
        Long poolSize = poolManager.getPoolSize();
        assertThat(poolSize).isEqualTo(0);
    }

    @Test
    @DisplayName("가장 오래된 N개 User-Agent를 조회할 수 있다")
    void canPeekLeastRecentlyUsed() throws InterruptedException {
        // Given
        poolManager.addToPool(1L);
        Thread.sleep(10);
        poolManager.addToPool(2L);
        Thread.sleep(10);
        poolManager.addToPool(3L);

        // When
        Set<Object> oldest = poolManager.peekLeastRecentlyUsed(2);

        // Then
        assertThat(oldest).hasSize(2);
        assertThat(oldest).contains("1", "2");
        assertThat(poolManager.getPoolSize()).isEqualTo(3); // 제거되지 않음
    }

    @Test
    @DisplayName("마지막 사용 시각을 조회할 수 있다")
    void canGetLastUsedTimestamp() {
        // Given
        Long userAgentId = 1L;
        poolManager.addToPool(userAgentId);

        // When
        Long timestamp = poolManager.getLastUsedTimestamp(userAgentId);

        // Then
        assertThat(timestamp).isNotNull();
        assertThat(timestamp).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    @Test
    @DisplayName("모든 User-Agent ID를 조회할 수 있다")
    void canGetAllUserAgentIds() {
        // Given
        poolManager.addToPool(1L);
        poolManager.addToPool(2L);
        poolManager.addToPool(3L);

        // When
        Set<Object> allIds = poolManager.getAllUserAgentIds();

        // Then
        assertThat(allIds).hasSize(3);
        assertThat(allIds).contains("1", "2", "3");
    }

    @Test
    @DisplayName("빈 Pool에서 acquire 시 null 반환")
    void returnsNullWhenPoolEmpty() {
        // When
        Long acquired = poolManager.acquireLeastRecentlyUsed();

        // Then
        assertThat(acquired).isNull();
    }
}
