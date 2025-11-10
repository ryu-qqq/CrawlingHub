package com.ryuqq.crawlinghub.adapter.redis.lock;

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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("DistributedLockService 테스트")
class DistributedLockServiceTest {

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private DistributedLockService lockService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("락을 획득할 수 있다")
    void canAcquireLock() {
        // Given
        String resourceId = "test_resource_1";

        // When
        DistributedLockService.LockHandle lock = lockService.tryLock(resourceId);

        // Then
        assertThat(lock.isAcquired()).isTrue();
        assertThat(lock.getLockOwner()).isNotNull();
    }

    @Test
    @DisplayName("이미 획득된 락은 다른 요청이 획득할 수 없다")
    void cannotAcquireAlreadyAcquiredLock() {
        // Given
        String resourceId = "test_resource_2";
        lockService.tryLock(resourceId);

        // When
        DistributedLockService.LockHandle secondLock = lockService.tryLock(resourceId);

        // Then
        assertThat(secondLock.isAcquired()).isFalse();
    }

    @Test
    @DisplayName("락을 해제할 수 있다")
    void canReleaseLock() {
        // Given
        String resourceId = "test_resource_3";
        DistributedLockService.LockHandle lock = lockService.tryLock(resourceId);

        // When
        boolean released = lockService.unlock(lock);

        // Then
        assertThat(released).isTrue();
        assertThat(lockService.isLocked(resourceId)).isFalse();
    }

    @Test
    @DisplayName("락 소유자만 해제할 수 있다")
    void onlyOwnerCanReleaseLock() {
        // Given
        String resourceId = "test_resource_4";
        DistributedLockService.LockHandle lock1 = lockService.tryLock(resourceId);

        // 다른 소유자로 위장한 핸들
        DistributedLockService.LockHandle fakeLock = new DistributedLockService.LockHandle(
            lock1.getLockKey(),
            "fake_owner",
            true
        );

        // When
        boolean released = lockService.unlock(fakeLock);

        // Then
        assertThat(released).isFalse();
        assertThat(lockService.isLocked(resourceId)).isTrue();
    }

    @Test
    @DisplayName("락 획득 후 해제하면 다시 획득 가능하다")
    void canReacquireAfterRelease() {
        // Given
        String resourceId = "test_resource_5";
        DistributedLockService.LockHandle lock1 = lockService.tryLock(resourceId);
        lockService.unlock(lock1);

        // When
        DistributedLockService.LockHandle lock2 = lockService.tryLock(resourceId);

        // Then
        assertThat(lock2.isAcquired()).isTrue();
    }

    @Test
    @DisplayName("동시성 테스트 - 하나의 락만 획득 가능")
    void concurrencyTest() throws InterruptedException {
        // Given
        String resourceId = "concurrent_resource";
        int threadCount = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    DistributedLockService.LockHandle lock = lockService.tryLock(resourceId);
                    if (lock.isAcquired()) {
                        successCount.incrementAndGet();
                        Thread.sleep(50); // 락 보유 시간
                        lockService.unlock(lock);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Then - 처음 한 번만 성공
        assertThat(successCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("TTL 지정하여 락 획득")
    void canAcquireLockWithCustomTTL() {
        // Given
        String resourceId = "test_resource_ttl";
        int ttlSeconds = 5;

        // When
        DistributedLockService.LockHandle lock = lockService.tryLock(resourceId, ttlSeconds);

        // Then
        assertThat(lock.isAcquired()).isTrue();
    }

    @Test
    @DisplayName("락 존재 여부 확인")
    void canCheckIfLocked() {
        // Given
        String resourceId = "test_resource_exists";

        // When - 락 획득 전
        boolean beforeLock = lockService.isLocked(resourceId);

        lockService.tryLock(resourceId);

        // When - 락 획득 후
        boolean afterLock = lockService.isLocked(resourceId);

        // Then
        assertThat(beforeLock).isFalse();
        assertThat(afterLock).isTrue();
    }
}
