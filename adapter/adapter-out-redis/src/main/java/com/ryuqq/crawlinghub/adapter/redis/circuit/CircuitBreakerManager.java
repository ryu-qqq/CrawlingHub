package com.ryuqq.crawlinghub.adapter.redis.circuit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Circuit Breaker Manager.
 * Redis Hash 기반 Circuit Breaker 상태 관리
 *
 * 상태:
 * - CLOSED: 정상 동작
 * - OPEN: 차단 (429 에러 연속 발생)
 * - HALF_OPEN: 복구 시도 중
 *
 * @author crawlinghub
 */
@Service
public class CircuitBreakerManager {

    private static final String CIRCUIT_KEY_PREFIX = "circuit_breaker:";
    private static final int DEFAULT_TTL_SECONDS = 3600; // 1시간
    private static final int DEFAULT_TIMEOUT_SECONDS = 600; // 10분
    private static final int DEFAULT_FAILURE_THRESHOLD = 3;

    private final RedisTemplate<String, Object> redisTemplate;

    public CircuitBreakerManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Circuit 상태 조회
     */
    public CircuitState getState(Long userAgentId) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;
        Map<Object, Object> data = redisTemplate.opsForHash().entries(circuitKey);

        if (data.isEmpty()) {
            return initializeCircuit(userAgentId);
        }

        String state = (String) data.get("state");
        int failureCount = Integer.parseInt(data.get("consecutive_failures").toString());
        int successCount = Integer.parseInt(data.get("consecutive_successes").toString());
        Long openedAt = data.get("opened_at") != null
                ? Long.parseLong(data.get("opened_at").toString())
                : null;
        int failureThreshold = Integer.parseInt(data.get("failure_threshold").toString());
        int timeoutSeconds = Integer.parseInt(data.get("timeout_duration_seconds").toString());

        return new CircuitState(
            CircuitStatus.valueOf(state),
            failureCount,
            successCount,
            openedAt,
            failureThreshold,
            timeoutSeconds
        );
    }

    /**
     * 성공 기록
     */
    public void recordSuccess(Long userAgentId) {
        CircuitState currentState = getState(userAgentId);

        if (currentState.getStatus() == CircuitStatus.HALF_OPEN) {
            // HALF_OPEN → CLOSED 전환
            int newSuccessCount = currentState.getConsecutiveSuccesses() + 1;
            if (newSuccessCount >= 3) {
                transitionToClosed(userAgentId);
            } else {
                incrementSuccessCount(userAgentId, newSuccessCount);
            }
        } else if (currentState.getStatus() == CircuitStatus.CLOSED) {
            // 실패 카운트 리셋
            resetFailureCount(userAgentId);
        }
    }

    /**
     * 실패 기록
     */
    public void recordFailure(Long userAgentId) {
        CircuitState currentState = getState(userAgentId);

        if (currentState.getStatus() == CircuitStatus.CLOSED) {
            int newFailureCount = currentState.getConsecutiveFailures() + 1;
            if (newFailureCount >= currentState.getFailureThreshold()) {
                transitionToOpen(userAgentId);
            } else {
                incrementFailureCount(userAgentId, newFailureCount);
            }
        } else if (currentState.getStatus() == CircuitStatus.HALF_OPEN) {
            // HALF_OPEN → OPEN 재전환
            transitionToOpen(userAgentId);
        }
    }

    /**
     * Circuit 복구 시도 (OPEN → HALF_OPEN)
     */
    public boolean tryRecover(Long userAgentId) {
        CircuitState currentState = getState(userAgentId);

        if (currentState.getStatus() != CircuitStatus.OPEN) {
            return false;
        }

        if (currentState.getOpenedAt() == null) {
            return false;
        }

        long elapsedSeconds = (System.currentTimeMillis() - currentState.getOpenedAt()) / 1000;
        if (elapsedSeconds >= currentState.getTimeoutDurationSeconds()) {
            transitionToHalfOpen(userAgentId);
            return true;
        }

        return false;
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private CircuitState initializeCircuit(Long userAgentId) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;

        redisTemplate.opsForHash().putAll(circuitKey, Map.of(
            "state", CircuitStatus.CLOSED.name(),
            "consecutive_failures", "0",
            "consecutive_successes", "0",
            "failure_threshold", String.valueOf(DEFAULT_FAILURE_THRESHOLD),
            "timeout_duration_seconds", String.valueOf(DEFAULT_TIMEOUT_SECONDS)
        ));
        redisTemplate.expire(circuitKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);

        return new CircuitState(
            CircuitStatus.CLOSED,
            0,
            0,
            null,
            DEFAULT_FAILURE_THRESHOLD,
            DEFAULT_TIMEOUT_SECONDS
        );
    }

    private void transitionToOpen(Long userAgentId) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;
        long now = System.currentTimeMillis();

        redisTemplate.opsForHash().putAll(circuitKey, Map.of(
            "state", CircuitStatus.OPEN.name(),
            "consecutive_failures", "0",
            "consecutive_successes", "0",
            "opened_at", String.valueOf(now)
        ));
        redisTemplate.expire(circuitKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private void transitionToHalfOpen(Long userAgentId) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;

        redisTemplate.opsForHash().putAll(circuitKey, Map.of(
            "state", CircuitStatus.HALF_OPEN.name(),
            "consecutive_failures", "0",
            "consecutive_successes", "0"
        ));
        redisTemplate.expire(circuitKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private void transitionToClosed(Long userAgentId) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;

        redisTemplate.opsForHash().putAll(circuitKey, Map.of(
            "state", CircuitStatus.CLOSED.name(),
            "consecutive_failures", "0",
            "consecutive_successes", "0"
        ));
        redisTemplate.opsForHash().delete(circuitKey, "opened_at");
        redisTemplate.expire(circuitKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private void incrementFailureCount(Long userAgentId, int count) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;
        redisTemplate.opsForHash().put(circuitKey, "consecutive_failures", String.valueOf(count));
        redisTemplate.expire(circuitKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private void incrementSuccessCount(Long userAgentId, int count) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;
        redisTemplate.opsForHash().put(circuitKey, "consecutive_successes", String.valueOf(count));
        redisTemplate.expire(circuitKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private void resetFailureCount(Long userAgentId) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;
        redisTemplate.opsForHash().put(circuitKey, "consecutive_failures", "0");
        redisTemplate.expire(circuitKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Circuit Status
     */
    public enum CircuitStatus {
        CLOSED,    // 정상 동작
        OPEN,      // 차단 상태 (429 에러 연속 발생)
        HALF_OPEN  // 복구 시도 중
    }

    /**
     * Circuit State.
     */
    public static class CircuitState {
        private final CircuitStatus status;
        private final int consecutiveFailures;
        private final int consecutiveSuccesses;
        private final Long openedAt;
        private final int failureThreshold;
        private final int timeoutDurationSeconds;

        /**
         * Constructor.
         *
         * @param status Circuit status
         * @param consecutiveFailures Consecutive failures count
         * @param consecutiveSuccesses Consecutive successes count
         * @param openedAt Opened timestamp
         * @param failureThreshold Failure threshold
         * @param timeoutDurationSeconds Timeout duration in seconds
         */
        public CircuitState(
                CircuitStatus status,
                int consecutiveFailures,
                int consecutiveSuccesses,
                Long openedAt,
                int failureThreshold,
                int timeoutDurationSeconds) {
            this.status = status;
            this.consecutiveFailures = consecutiveFailures;
            this.consecutiveSuccesses = consecutiveSuccesses;
            this.openedAt = openedAt;
            this.failureThreshold = failureThreshold;
            this.timeoutDurationSeconds = timeoutDurationSeconds;
        }

        public CircuitStatus getStatus() {
            return status;
        }

        public int getConsecutiveFailures() {
            return consecutiveFailures;
        }

        public int getConsecutiveSuccesses() {
            return consecutiveSuccesses;
        }

        public Long getOpenedAt() {
            return openedAt;
        }

        public int getFailureThreshold() {
            return failureThreshold;
        }

        public int getTimeoutDurationSeconds() {
            return timeoutDurationSeconds;
        }

        public boolean isOpen() {
            return status == CircuitStatus.OPEN;
        }

        public boolean isHalfOpen() {
            return status == CircuitStatus.HALF_OPEN;
        }

        public boolean isClosed() {
            return status == CircuitStatus.CLOSED;
        }
    }
}
