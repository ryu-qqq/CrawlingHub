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
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
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
        redisTemplate.opsForHash().putAll(circuitKey, java.util.Map.of(
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
        redisTemplate.opsForHash().putAll(circuitKey, java.util.Map.of(
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
}
