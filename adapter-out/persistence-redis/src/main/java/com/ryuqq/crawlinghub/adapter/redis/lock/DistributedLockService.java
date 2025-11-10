package com.ryuqq.crawlinghub.adapter.redis.lock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * Distributed Lock Service
 * Lua Script 기반 분산 락
 *
 * 특징:
 * - SET NX EX 기반 락 획득
 * - 락 소유자만 해제 가능
 * - 락 타임아웃 처리 (30초)
 * - 데드락 방지 메커니즘
  *
 * @author crawlinghub
 */
@Service
public class DistributedLockService {

    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final int DEFAULT_LOCK_TTL_SECONDS = 30;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Long> distributedLockScript;
    private final RedisScript<Long> distributedUnlockScript;

    public DistributedLockService(
            RedisTemplate<String, Object> redisTemplate,
            RedisScript<Long> distributedLockScript,
            RedisScript<Long> distributedUnlockScript) {
        this.redisTemplate = redisTemplate;
        this.distributedLockScript = distributedLockScript;
        this.distributedUnlockScript = distributedUnlockScript;
    }

    /**
     * 락 획득 시도
     *
     * @param resourceId 리소스 ID
     * @return LockHandle (락 획득 성공 시)
      *
 * @author crawlinghub
 */
    public LockHandle tryLock(String resourceId) {
        return tryLock(resourceId, DEFAULT_LOCK_TTL_SECONDS);
    }

    /**
     * 락 획득 시도 (TTL 지정)
     *
     * @param resourceId 리소스 ID
     * @param ttlSeconds 락 타임아웃 (초)
     * @return LockHandle (락 획득 성공 시)
      *
 * @author crawlinghub
 */
    public LockHandle tryLock(String resourceId, int ttlSeconds) {
        String lockKey = LOCK_KEY_PREFIX + resourceId;
        String lockOwner = UUID.randomUUID().toString();

        Long result = redisTemplate.execute(
            distributedLockScript,
            Collections.singletonList(lockKey),
            lockOwner,
            ttlSeconds
        );

        if (result != null && result == 1) {
            return new LockHandle(lockKey, lockOwner, true);
        } else {
            return new LockHandle(lockKey, null, false);
        }
    }

    /**
     * 락 해제
     *
     * @param lockHandle 락 핸들
     * @return 성공 여부
      *
 * @author crawlinghub
 */
    public boolean unlock(LockHandle lockHandle) {
        if (!lockHandle.isAcquired()) {
            return false;
        }

        Long result = redisTemplate.execute(
            distributedUnlockScript,
            Collections.singletonList(lockHandle.getLockKey()),
            lockHandle.getLockOwner()
        );

        return result != null && result == 1;
    }

    /**
     * 락이 존재하는지 확인
      *
 * @author crawlinghub
 */
    public boolean isLocked(String resourceId) {
        String lockKey = LOCK_KEY_PREFIX + resourceId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * Lock Handle
      *
 * @author crawlinghub
 */
    public static class LockHandle {
        private final String lockKey;
        private final String lockOwner;
        private final boolean acquired;

        public LockHandle(String lockKey, String lockOwner, boolean acquired) {
            this.lockKey = lockKey;
            this.lockOwner = lockOwner;
            this.acquired = acquired;
        }

        public String getLockKey() {
            return lockKey;
        }

        public String getLockOwner() {
            return lockOwner;
        }

        public boolean isAcquired() {
            return acquired;
        }

        @Override
        public String toString() {
            return "LockHandle{" +
                    "lockKey='" + lockKey + '\'' +
                    ", acquired=" + acquired +
                    '}';
        }
    }
}
