package com.ryuqq.crawlinghub.adapter.redis.lock;

import com.ryuqq.crawlinghub.application.token.port.DistributedLockPort;
import org.springframework.stereotype.Component;

/**
 * Distributed Lock Adapter
 * <p>
 * Redis 기반 분산 락 관리
 * - 동시성 제어를 위한 Lock 획득/해제
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class DistributedLockAdapter implements DistributedLockPort {

    private final RedisDistributedLock distributedLock;

    public DistributedLockAdapter(RedisDistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    /**
     * 락 획득 시도
     *
     * @param lockKey 락 키
     * @param timeoutMs 타임아웃 (밀리초)
     * @return 락 값 (획득 실패 시 null)
     */
    @Override
    public String tryAcquire(String lockKey, long timeoutMs) {
        // RedisDistributedLock은 기본적으로 timeout을 내부적으로 처리
        // 여기서는 간단하게 tryAcquire 호출
        RedisDistributedLock.LockHandle lockHandle = distributedLock.tryAcquire(lockKey);
        return lockHandle != null ? lockHandle.getLockValue() : null;
    }

    /**
     * 락 해제
     *
     * @param lockKey 락 키
     * @param lockValue 락 값
     */
    @Override
    public void release(String lockKey, String lockValue) {
        distributedLock.release(lockKey, lockValue);
    }
}
