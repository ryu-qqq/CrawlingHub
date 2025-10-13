package com.ryuqq.crawlinghub.application.token.port;

/**
 * Distributed Lock Port (Outbound)
 *
 * @author crawlinghub
 */
public interface DistributedLockPort {

    /**
     * 락 획득 시도
     *
     * @param lockKey 락 키
     * @return lockValue (성공) 또는 null (실패)
     */
    String tryAcquire(String lockKey);

    /**
     * 락 해제
     *
     * @param lockKey 락 키
     * @param lockValue 락 값
     * @return 해제 성공 여부
     */
    boolean release(String lockKey, String lockValue);
}
