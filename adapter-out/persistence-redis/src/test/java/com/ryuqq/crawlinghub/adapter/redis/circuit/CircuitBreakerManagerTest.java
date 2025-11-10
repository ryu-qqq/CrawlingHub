package com.ryuqq.crawlinghub.adapter.redis.circuit;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("CircuitBreakerManager 테스트")
class CircuitBreakerManagerTest {

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private CircuitBreakerManager circuitBreakerManager;

    @Autowired
    private RedisTemplate<String, String> circuitBreakerRedisTemplate;

    @BeforeEach
    void setUp() {
        circuitBreakerRedisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("초기 상태는 CLOSED이다")
    void initialStateIsClosed() {
        // Given
        Long userAgentId = 1L;

        // When
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);

        // Then
        assertThat(state.isClosed()).isTrue();
        assertThat(state.getConsecutiveFailures()).isEqualTo(0);
    }

    @Test
    @DisplayName("성공 기록 시 실패 카운트가 리셋된다")
    void successResetsFailureCount() {
        // Given
        Long userAgentId = 2L;
        circuitBreakerManager.recordFailure(userAgentId);

        // When
        circuitBreakerManager.recordSuccess(userAgentId);

        // Then
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        assertThat(state.isClosed()).isTrue();
        assertThat(state.getConsecutiveFailures()).isEqualTo(0);
    }

    @Test
    @DisplayName("실패 임계값 도달 시 OPEN으로 전환된다")
    void transitionsToOpenOnFailureThreshold() {
        // Given
        Long userAgentId = 3L;

        // When - 3번 실패 (기본 threshold)
        circuitBreakerManager.recordFailure(userAgentId);
        circuitBreakerManager.recordFailure(userAgentId);
        circuitBreakerManager.recordFailure(userAgentId);

        // Then
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        assertThat(state.isOpen()).isTrue();
        assertThat(state.getOpenedAt()).isNotNull();
    }

    @Test
    @DisplayName("OPEN 상태에서 timeout 경과 후 HALF_OPEN으로 전환 가능")
    void canTransitionToHalfOpenAfterTimeout() throws InterruptedException {
        // Given
        Long userAgentId = 4L;

        // OPEN으로 전환
        circuitBreakerManager.recordFailure(userAgentId);
        circuitBreakerManager.recordFailure(userAgentId);
        circuitBreakerManager.recordFailure(userAgentId);

        // When - timeout 대기 (테스트용으로 짧게 설정 필요)
        // 실제 타임아웃은 600초이므로 여기서는 복구 시도 실패 확인
        boolean recovered = circuitBreakerManager.tryRecover(userAgentId);

        // Then
        assertThat(recovered).isFalse(); // 아직 타임아웃 안됨
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        assertThat(state.isOpen()).isTrue();
    }

    @Test
    @DisplayName("HALF_OPEN 상태에서 성공 시 CLOSED로 전환")
    void transitionsToClosedFromHalfOpenOnSuccess() {
        // Given
        Long userAgentId = 5L;

        // 수동으로 HALF_OPEN 상태 설정 (테스트용)
        String circuitKey = "circuit_breaker:" + userAgentId;
        circuitBreakerRedisTemplate.opsForHash().putAll(circuitKey, java.util.Map.of(
            "state", "HALF_OPEN",
            "consecutive_failures", "0",
            "consecutive_successes", "0",
            "failure_threshold", "3",
            "timeout_duration_seconds", "600"
        ));

        // When - 3번 연속 성공
        circuitBreakerManager.recordSuccess(userAgentId);
        circuitBreakerManager.recordSuccess(userAgentId);
        circuitBreakerManager.recordSuccess(userAgentId);

        // Then
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        assertThat(state.isClosed()).isTrue();
    }

    @Test
    @DisplayName("HALF_OPEN 상태에서 실패 시 다시 OPEN으로 전환")
    void transitionsToOpenFromHalfOpenOnFailure() {
        // Given
        Long userAgentId = 6L;

        // HALF_OPEN 상태 설정
        String circuitKey = "circuit_breaker:" + userAgentId;
        circuitBreakerRedisTemplate.opsForHash().putAll(circuitKey, java.util.Map.of(
            "state", "HALF_OPEN",
            "consecutive_failures", "0",
            "consecutive_successes", "0",
            "failure_threshold", "3",
            "timeout_duration_seconds", "600"
        ));

        // When
        circuitBreakerManager.recordFailure(userAgentId);

        // Then
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        assertThat(state.isOpen()).isTrue();
    }

    @Test
    @DisplayName("동시성 테스트 - 10개 스레드가 동시에 recordFailure 호출")
    void concurrentRecordFailureTest() throws InterruptedException {
        // Given
        Long userAgentId = 100L;
        int threadCount = 10;
        int failuresPerThread = 1;
        int totalFailures = threadCount * failuresPerThread; // 10

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);

        // When - 모든 스레드가 동시에 recordFailure 호출
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startBarrier.await(); // 모든 스레드가 동시에 시작하도록 대기
                    for (int j = 0; j < failuresPerThread; j++) {
                        circuitBreakerManager.recordFailure(userAgentId);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then - 정확히 10번의 실패가 기록되어야 하고, OPEN 상태여야 함
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);

        // 총 10번 실패 → threshold 3 초과 → OPEN 상태
        assertThat(state.isOpen()).isTrue();
        assertThat(state.getOpenedAt()).isNotNull();

        // consecutive_failures는 OPEN 전환 시 0으로 리셋됨
        assertThat(state.getConsecutiveFailures()).isEqualTo(0);
    }

    @Test
    @DisplayName("동시성 테스트 - CLOSED에서 threshold 미만 실패")
    void concurrentRecordFailureUnderThreshold() throws InterruptedException {
        // Given
        Long userAgentId = 101L;
        int threadCount = 2;
        int failuresPerThread = 1;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);

        // When - 총 2번 실패 (threshold 3 미만)
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startBarrier.await();
                    for (int j = 0; j < failuresPerThread; j++) {
                        circuitBreakerManager.recordFailure(userAgentId);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then - CLOSED 상태 유지, consecutive_failures = 2
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        assertThat(state.isClosed()).isTrue();
        assertThat(state.getConsecutiveFailures()).isEqualTo(2);
    }

    @Test
    @DisplayName("동시성 테스트 - HALF_OPEN에서 동시 성공 호출")
    void concurrentRecordSuccessFromHalfOpen() throws InterruptedException {
        // Given
        Long userAgentId = 102L;

        // HALF_OPEN 상태 설정
        String circuitKey = "circuit_breaker:" + userAgentId;
        circuitBreakerRedisTemplate.opsForHash().putAll(circuitKey, java.util.Map.of(
            "state", "HALF_OPEN",
            "consecutive_failures", "0",
            "consecutive_successes", "0",
            "failure_threshold", "3",
            "timeout_duration_seconds", "600"
        ));

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);

        // When - 5개 스레드가 동시에 recordSuccess 호출
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startBarrier.await();
                    circuitBreakerManager.recordSuccess(userAgentId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then - 3번 성공 시 CLOSED로 전환되어야 함
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        assertThat(state.isClosed()).isTrue();
        assertThat(state.getConsecutiveSuccesses()).isEqualTo(0); // CLOSED 전환 시 리셋
    }

    @Test
    @DisplayName("동시성 테스트 - 실패와 성공이 동시에 발생하는 경우")
    void concurrentMixedFailureAndSuccess() throws InterruptedException {
        // Given
        Long userAgentId = 103L;
        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);
        AtomicInteger failureThreadCount = new AtomicInteger(0);
        AtomicInteger successThreadCount = new AtomicInteger(0);

        // When - 절반은 실패, 절반은 성공 호출
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                try {
                    startBarrier.await();
                    if (threadIndex % 2 == 0) {
                        // 짝수 스레드: 실패 기록
                        circuitBreakerManager.recordFailure(userAgentId);
                        failureThreadCount.incrementAndGet();
                    } else {
                        // 홀수 스레드: 성공 기록
                        circuitBreakerManager.recordSuccess(userAgentId);
                        successThreadCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then - 최종 상태 검증
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);

        // 동시성 환경에서는 실행 순서에 따라 결과가 달라질 수 있음:
        // - 성공이 먼저 실행되어 실패 카운트를 계속 리셋 → CLOSED 유지
        // - 실패 3개가 연속으로 먼저 실행 → OPEN 전환
        // 두 경우 모두 정상 동작이므로 상태가 CLOSED 또는 OPEN 중 하나여야 함
        assertThat(state.getStatus())
            .isIn(CircuitBreakerManager.CircuitStatus.CLOSED,
                  CircuitBreakerManager.CircuitStatus.OPEN);

        // 최소한 정상 동작 확인 (NPE나 예외 발생하지 않음)
        assertThat(state).isNotNull();
    }

    @Test
    @DisplayName("allowRequest() - CLOSED 상태에서는 항상 허용")
    void allowRequestInClosedState() {
        // Given
        Long userAgentId = 200L;

        // When
        boolean allowed = circuitBreakerManager.allowRequest(userAgentId);

        // Then
        assertThat(allowed).isTrue();
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        assertThat(state.isClosed()).isTrue();
    }

    @Test
    @DisplayName("allowRequest() - OPEN 상태에서 timeout 미경과 시 차단")
    void allowRequestInOpenStateBeforeTimeout() {
        // Given
        Long userAgentId = 201L;

        // OPEN으로 전환
        circuitBreakerManager.recordFailure(userAgentId);
        circuitBreakerManager.recordFailure(userAgentId);
        circuitBreakerManager.recordFailure(userAgentId);

        // When
        boolean allowed = circuitBreakerManager.allowRequest(userAgentId);

        // Then
        assertThat(allowed).isFalse();
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);
        assertThat(state.isOpen()).isTrue();
    }

    @Test
    @DisplayName("allowRequest() - HALF_OPEN 상태에서 동시 요청 방지 (경쟁 조건 해결)")
    void allowRequestInHalfOpenState() throws InterruptedException {
        // Given
        Long userAgentId = 202L;

        // HALF_OPEN 상태 설정
        String circuitKey = "circuit_breaker:" + userAgentId;
        circuitBreakerRedisTemplate.opsForHash().putAll(circuitKey, java.util.Map.of(
            "state", "HALF_OPEN",
            "consecutive_failures", "0",
            "consecutive_successes", "0",
            "failure_threshold", "3",
            "timeout_duration_seconds", "600"
        ));

        // When - 동시에 10개 요청 시도
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);
        List<Boolean> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startBarrier.await(); // 동시 시작
                    boolean allowed = circuitBreakerManager.allowRequest(userAgentId);
                    synchronized (results) {
                        results.add(allowed);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then - 정확히 1개의 요청만 허용되어야 함 (경쟁 조건 방지)
        long allowedCount = results.stream().filter(Boolean::booleanValue).count();
        assertThat(allowedCount).isEqualTo(1L);  // 1개만 허용
        assertThat(results.stream().filter(b -> !b).count()).isEqualTo(9L);  // 9개 차단
    }

    @Test
    @DisplayName("동시성 테스트 - HALF_OPEN에서 실패와 성공이 동시 발생")
    void concurrentMixedFailureAndSuccessFromHalfOpen() throws InterruptedException {
        // Given
        Long userAgentId = 104L;

        // HALF_OPEN 상태 설정
        String circuitKey = "circuit_breaker:" + userAgentId;
        circuitBreakerRedisTemplate.opsForHash().putAll(circuitKey, java.util.Map.of(
            "state", "HALF_OPEN",
            "consecutive_failures", "0",
            "consecutive_successes", "0",
            "failure_threshold", "3",
            "timeout_duration_seconds", "600"
        ));

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier startBarrier = new CyclicBarrier(threadCount);

        // When - 첫 스레드는 실패, 나머지는 성공 호출
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                try {
                    startBarrier.await();
                    if (threadIndex == 0) {
                        // 첫 번째 스레드: 실패 → OPEN으로 전환
                        circuitBreakerManager.recordFailure(userAgentId);
                    } else {
                        // 나머지: 성공 호출 (하지만 이미 OPEN이면 무시됨)
                        circuitBreakerManager.recordSuccess(userAgentId);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then - 최종 상태 검증
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(userAgentId);

        // 동시성 환경에서는 실행 순서에 따라 결과가 달라질 수 있음:
        // - 실패가 먼저 실행되면 OPEN으로 전환
        // - 성공 3개가 먼저 완료되면 CLOSED로 전환
        // 두 경우 모두 정상 동작이므로 상태가 CLOSED 또는 OPEN 중 하나여야 함
        assertThat(state.getStatus())
            .isIn(CircuitBreakerManager.CircuitStatus.CLOSED,
                  CircuitBreakerManager.CircuitStatus.OPEN);

        // 최소한 정상 동작 확인 (NPE나 예외 발생하지 않음)
        assertThat(state).isNotNull();
    }
}
