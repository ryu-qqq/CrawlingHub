package com.ryuqq.crawlinghub.adapter.redis.lock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis Distributed Lock
 * SET NX EX 기반 분산 락 구현
 *
 * 특징:
 * - 락 소유자만 해제 가능 (UUID 기반)
 * - 자동 만료 (Deadlock 방지)
 * - Lua Script로 원자성 보장
 *
 * @author crawlinghub
 */
@Component
public class RedisDistributedLock {

    private static final String LOCK_KEY_PREFIX = "distributed_lock:";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_RETRY_MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 100;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Long> unlockScript;

    public RedisDistributedLock(
            RedisTemplate<String, Object> redisTemplate,
            RedisScript<Long> distributedUnlockScript) {
        this.redisTemplate = redisTemplate;
        this.unlockScript = distributedUnlockScript;
    }

    /**
     * 락 획득 시도 (재시도 포함)
     *
     * @param lockKey 락 키
     * @return LockHandle (락 획득 성공 시) 또는 null
     */
    public LockHandle tryAcquire(String lockKey) {
        return tryAcquire(lockKey, DEFAULT_TIMEOUT_SECONDS, DEFAULT_RETRY_MAX_ATTEMPTS);
    }

    /**
     * 락 획득 시도 (재시도 포함, 타임아웃 지정)
     *
     * @param lockKey 락 키
     * @param timeoutSeconds 락 타임아웃 (초)
     * @param maxRetries 최대 재시도 횟수
     * @return LockHandle (락 획득 성공 시) 또는 null
     */
    public LockHandle tryAcquire(String lockKey, int timeoutSeconds, int maxRetries) {
        String fullKey = LOCK_KEY_PREFIX + lockKey;
        String lockValue = UUID.randomUUID().toString();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            // SET NX EX: key가 없으면 set하고 true 반환, 있으면 false 반환
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(fullKey, lockValue, timeoutSeconds, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(acquired)) {
                return new LockHandle(fullKey, lockValue, this);
            }

            if (attempt < maxRetries) {
                try {
                    // Exponential backoff
                    long backoffTime = RETRY_BACKOFF_MS * (1L << attempt);
                    Thread.sleep(backoffTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * 락 해제
     * Lua Script로 소유자만 해제 가능하도록 보장
     *
     * @param lockKey 락 키
     * @param lockValue 락 값 (UUID)
     * @return 해제 성공 여부
     */
    public boolean release(String lockKey, String lockValue) {
        Long result = redisTemplate.execute(
                unlockScript,
                Collections.singletonList(lockKey),
                lockValue
        );
        return result != null && result == 1L;
    }

    /**
     * Lock Handle
     * try-with-resources 사용 가능
     */
    public static class LockHandle implements AutoCloseable {
        private final String lockKey;
        private final String lockValue;
        private final RedisDistributedLock lock;
        private boolean released = false;

        public LockHandle(String lockKey, String lockValue, RedisDistributedLock lock) {
            this.lockKey = lockKey;
            this.lockValue = lockValue;
            this.lock = lock;
        }

        @Override
        public void close() {
            if (!released) {
                lock.release(lockKey, lockValue);
                released = true;
            }
        }

        public String getLockKey() {
            return lockKey;
        }

        public String getLockValue() {
            return lockValue;
        }

        public boolean isReleased() {
            return released;
        }
    }
}
