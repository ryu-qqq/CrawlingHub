package com.ryuqq.crawlinghub.adapter.redis.lock;

import com.ryuqq.crawlinghub.application.token.port.DistributedLockPort;
import org.springframework.stereotype.Component;

/**
 * Distributed Lock Adapter
 *
 * @author crawlinghub
 */
@Component
public class DistributedLockAdapter implements DistributedLockPort {

    private final RedisDistributedLock distributedLock;

    public DistributedLockAdapter(RedisDistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    @Override
    public String tryAcquire(String lockKey) {
        RedisDistributedLock.LockHandle lockHandle = distributedLock.tryAcquire(lockKey);
        return lockHandle != null ? lockHandle.getLockValue() : null;
    }

    @Override
    public boolean release(String lockKey, String lockValue) {
        return distributedLock.release(lockKey, lockValue);
    }
}
