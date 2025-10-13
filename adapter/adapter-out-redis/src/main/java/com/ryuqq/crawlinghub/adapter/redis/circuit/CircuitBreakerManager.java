package com.ryuqq.crawlinghub.adapter.redis.circuit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Circuit Breaker Manager.
 * Redis Hash + Lua Script 기반 Circuit Breaker 상태 관리 (Atomic Operations)
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

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<String> recordFailureScript;
    private final DefaultRedisScript<String> recordSuccessScript;

    public CircuitBreakerManager(RedisTemplate<String, String> circuitBreakerRedisTemplate) {
        this.redisTemplate = circuitBreakerRedisTemplate;
        this.recordFailureScript = loadScript("lua/record_failure.lua");
        this.recordSuccessScript = loadScript("lua/record_success.lua");
    }

    private DefaultRedisScript<String> loadScript(String scriptPath) {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(scriptPath)));
        script.setResultType(String.class);
        return script;
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
        int failureCount = Integer.parseInt((String) data.get("consecutive_failures"));
        int successCount = Integer.parseInt((String) data.get("consecutive_successes"));
        Long openedAt = data.get("opened_at") != null
                ? Long.parseLong((String) data.get("opened_at"))
                : null;
        int failureThreshold = Integer.parseInt((String) data.get("failure_threshold"));
        int timeoutSeconds = Integer.parseInt((String) data.get("timeout_duration_seconds"));

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
     * 성공 기록 (Atomic - Lua Script)
     * HALF_OPEN → CLOSED 전환 또는 CLOSED 상태에서 실패 카운터 리셋
     */
    public void recordSuccess(Long userAgentId) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;

        // Lua script 실행 (atomic operation)
        redisTemplate.execute(
            recordSuccessScript,
            List.of(circuitKey),
            String.valueOf(DEFAULT_TTL_SECONDS)
        );
    }

    /**
     * 실패 기록 (Atomic - Lua Script)
     * CLOSED → OPEN 전환 또는 HALF_OPEN → OPEN 재전환
     */
    public void recordFailure(Long userAgentId) {
        String circuitKey = CIRCUIT_KEY_PREFIX + userAgentId;

        // Lua script 실행 (atomic operation)
        redisTemplate.execute(
            recordFailureScript,
            List.of(circuitKey),
            String.valueOf(DEFAULT_FAILURE_THRESHOLD),
            String.valueOf(System.currentTimeMillis()),
            String.valueOf(DEFAULT_TTL_SECONDS)
        );
    }

    /**
     * 요청 허용 여부 확인 (Jira CRAW-83 요구사항)
     * - CLOSED: 항상 허용 (return true)
     * - OPEN: timeout 체크 후 HALF_OPEN 전이 또는 차단
     * - HALF_OPEN: 테스트 요청 1개만 허용 (consecutive_successes == 0)
     */
    public boolean allowRequest(Long userAgentId) {
        CircuitState state = getState(userAgentId);

        switch (state.getStatus()) {
            case CLOSED:
                // 정상 상태: 항상 허용
                return true;

            case OPEN:
                // 차단 상태: timeout 경과 확인
                if (state.getOpenedAt() == null) {
                    return false;
                }

                long elapsedSeconds = (System.currentTimeMillis() - state.getOpenedAt()) / 1000;
                if (elapsedSeconds >= state.getTimeoutDurationSeconds()) {
                    // Timeout 경과: HALF_OPEN으로 전환하고 허용
                    transitionToHalfOpen(userAgentId);
                    return true;
                } else {
                    // Timeout 미경과: 차단
                    return false;
                }

            case HALF_OPEN:
                // 복구 시도 중: 테스트 요청 1개만 허용
                return state.getConsecutiveSuccesses() == 0;

            default:
                return false;
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

    /**
     * Circuit Breaker 수동 리셋 (관리자 기능)
     */
    public void reset(Long userAgentId, String reason) {
        transitionToClosed(userAgentId);
        // TODO: circuit_breaker_event 로깅 추가 예정
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
