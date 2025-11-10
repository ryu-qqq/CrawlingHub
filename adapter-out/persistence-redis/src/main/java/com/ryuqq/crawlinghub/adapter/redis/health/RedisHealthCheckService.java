package com.ryuqq.crawlinghub.adapter.redis.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis Health Check Service
 * - Connection 상태 모니터링
 * - Retry 정책
 * - Failover 감지
  *
 * @author crawlinghub
 */
@Service
public class RedisHealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(RedisHealthCheckService.class);
    private static final String HEALTH_CHECK_KEY = "health_check:ping";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory connectionFactory;

    public RedisHealthCheckService(
            RedisTemplate<String, Object> redisTemplate,
            RedisConnectionFactory connectionFactory) {
        this.redisTemplate = redisTemplate;
        this.connectionFactory = connectionFactory;
    }

    /**
     * Redis 연결 상태 확인
     *
     * @return HealthStatus
      *
 * @author crawlinghub
 */
    public HealthStatus checkHealth() {
        long startTime = System.currentTimeMillis();

        try {
            // PING 테스트
            String pingResult = executePing();
            long responseTime = System.currentTimeMillis() - startTime;

            if ("PONG".equals(pingResult)) {
                return new HealthStatus(
                    true,
                    responseTime,
                    "Redis connection healthy",
                    null
                );
            } else {
                return new HealthStatus(
                    false,
                    responseTime,
                    "Unexpected ping response: " + pingResult,
                    null
                );
            }
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Redis health check failed", e);

            return new HealthStatus(
                false,
                responseTime,
                "Health check failed",
                e.getMessage()
            );
        }
    }

    /**
     * Redis PING with Retry
      *
 * @author crawlinghub
 */
    public String executePing() {
        return executeWithRetry(() -> {
            try (RedisConnection connection = connectionFactory.getConnection()) {
                return connection.ping();
            }
        });
    }

    /**
     * SET/GET 테스트 with Retry
      *
 * @author crawlinghub
 */
    public boolean testSetGet() {
        return executeWithRetry(() -> {
            String testValue = "test_" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(HEALTH_CHECK_KEY, testValue, 10, TimeUnit.SECONDS);

            Object result = redisTemplate.opsForValue().get(HEALTH_CHECK_KEY);
            return testValue.equals(result);
        });
    }

    /**
     * Connection Pool 상태 조회
      *
 * @author crawlinghub
 */
    public ConnectionPoolStatus getConnectionPoolStatus() {
        // Lettuce Connection Pool 상태는 직접 노출되지 않으므로
        // Active Connection 테스트로 대체
        try {
            boolean pingSuccess = "PONG".equals(executePing());
            return new ConnectionPoolStatus(
                pingSuccess,
                "Connection pool operational",
                null
            );
        } catch (Exception e) {
            return new ConnectionPoolStatus(
                false,
                "Connection pool check failed",
                e.getMessage()
            );
        }
    }

    /**
     * Retry 로직을 적용한 실행
      *
 * @author crawlinghub
 */
    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                attempts++;

                if (attempts < MAX_RETRY_ATTEMPTS) {
                    log.warn("Redis operation failed (attempt {}/{}), retrying...",
                             attempts, MAX_RETRY_ATTEMPTS, e);

                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        throw new RuntimeException("Redis operation failed after " + MAX_RETRY_ATTEMPTS + " attempts",
                                   lastException);
    }

    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Health Status
      *
 * @author crawlinghub
 */
    public static class HealthStatus {
        private final boolean healthy;
        private final long responseTimeMs;
        private final String message;
        private final String errorDetails;

        public HealthStatus(boolean healthy, long responseTimeMs, String message, String errorDetails) {
            this.healthy = healthy;
            this.responseTimeMs = responseTimeMs;
            this.message = message;
            this.errorDetails = errorDetails;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public String getMessage() {
            return message;
        }

        public String getErrorDetails() {
            return errorDetails;
        }

        @Override
        public String toString() {
            return "HealthStatus{" +
                    "healthy=" + healthy +
                    ", responseTimeMs=" + responseTimeMs +
                    ", message='" + message + '\'' +
                    ", errorDetails='" + errorDetails + '\'' +
                    '}';
        }
    }

    /**
     * Connection Pool Status
      *
 * @author crawlinghub
 */
    public static class ConnectionPoolStatus {
        private final boolean operational;
        private final String message;
        private final String errorDetails;

        public ConnectionPoolStatus(boolean operational, String message, String errorDetails) {
            this.operational = operational;
            this.message = message;
            this.errorDetails = errorDetails;
        }

        public boolean isOperational() {
            return operational;
        }

        public String getMessage() {
            return message;
        }

        public String getErrorDetails() {
            return errorDetails;
        }
    }
}
