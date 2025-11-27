package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import com.ryuqq.crawlinghub.adapter.out.redis.config.RedisProperties;
import com.ryuqq.crawlinghub.application.common.port.out.lock.DistributedLockPort;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Redis 분산 락 Adapter
 *
 * <p><strong>용도</strong>: Redisson을 사용한 분산 락 구현
 *
 * <p><strong>Redisson RLock 특징</strong>:
 *
 * <ul>
 *   <li>Reentrant Lock: 같은 스레드에서 중첩 획득 가능
 *   <li>Watchdog: leaseTime이 -1이면 자동으로 락 갱신 (30초마다)
 *   <li>Pub/Sub 기반 대기: 락 해제 시 즉시 알림
 *   <li>Fairness: 대기 순서 보장
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class RedisDistributedLockAdapter implements DistributedLockPort {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLockAdapter.class);

    private final RedissonClient redissonClient;
    private final RedisProperties redisProperties;

    public RedisDistributedLockAdapter(
            RedissonClient redissonClient, RedisProperties redisProperties) {
        this.redissonClient = redissonClient;
        this.redisProperties = redisProperties;
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        String fullKey = buildFullKey(lockKey);
        RLock lock = redissonClient.getLock(fullKey);

        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (acquired) {
                log.debug("Distributed lock acquired: key={}", fullKey);
            } else {
                log.debug("Failed to acquire distributed lock: key={}", fullKey);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock acquisition interrupted: key={}", fullKey, e);
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        String fullKey = buildFullKey(lockKey);
        RLock lock = redissonClient.getLock(fullKey);

        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Distributed lock released: key={}", fullKey);
            } else {
                log.warn("Attempted to unlock a lock not held by current thread: key={}", fullKey);
            }
        } catch (IllegalMonitorStateException e) {
            log.warn("Failed to unlock (lock already released or not held): key={}", fullKey, e);
        }
    }

    @Override
    public <T> T executeWithLock(
            String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> action) {
        String fullKey = buildFullKey(lockKey);
        RLock lock = redissonClient.getLock(fullKey);

        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!acquired) {
                log.warn("Failed to acquire lock for execution: key={}", fullKey);
                return null;
            }

            try {
                log.debug("Executing action with lock: key={}", fullKey);
                return action.get();
            } catch (RuntimeException e) {
                // 비즈니스 로직 예외는 그대로 전파 (DLQ 등 후속 처리를 위해)
                log.error(
                        "Action execution failed with lock: key={}, error={}",
                        fullKey,
                        e.getMessage());
                throw e;
            } finally {
                unlockSafely(lock, fullKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted during execution: key={}", fullKey, e);
            return null;
        }
    }

    @Override
    public boolean executeWithLock(
            String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable action) {
        String fullKey = buildFullKey(lockKey);
        RLock lock = redissonClient.getLock(fullKey);

        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!acquired) {
                log.warn("Failed to acquire lock for execution: key={}", fullKey);
                return false;
            }

            try {
                log.debug("Executing action with lock: key={}", fullKey);
                action.run();
                return true;
            } catch (RuntimeException e) {
                // 비즈니스 로직 예외는 그대로 전파 (DLQ 등 후속 처리를 위해)
                log.error(
                        "Action execution failed with lock: key={}, error={}",
                        fullKey,
                        e.getMessage());
                throw e;
            } finally {
                unlockSafely(lock, fullKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted during execution: key={}", fullKey, e);
            return false;
        }
    }

    @Override
    public boolean isLocked(String lockKey) {
        String fullKey = buildFullKey(lockKey);
        RLock lock = redissonClient.getLock(fullKey);
        return lock.isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread(String lockKey) {
        String fullKey = buildFullKey(lockKey);
        RLock lock = redissonClient.getLock(fullKey);
        return lock.isHeldByCurrentThread();
    }

    /**
     * 락 키에 prefix 추가
     *
     * @param lockKey 락 키
     * @return prefix가 추가된 전체 키
     */
    private String buildFullKey(String lockKey) {
        return redisProperties.getKeyPrefix() + lockKey;
    }

    /**
     * 락 안전하게 해제
     *
     * @param lock RLock 인스턴스
     * @param fullKey 전체 키 (로깅용)
     */
    private void unlockSafely(RLock lock, String fullKey) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released after execution: key={}", fullKey);
            }
        } catch (IllegalMonitorStateException e) {
            log.warn("Failed to release lock (already released): key={}", fullKey);
        }
    }
}
