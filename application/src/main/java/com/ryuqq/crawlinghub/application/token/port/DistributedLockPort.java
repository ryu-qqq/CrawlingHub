package com.ryuqq.crawlinghub.application.token.port;

/**
 * 분산 락 Port
 * <p>
 * - Redis 기반 분산 락 관리
 * - 동시성 제어를 위한 Lock 획득/해제
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface DistributedLockPort {

    /**
     * 락 획득 시도
     *
     * @param lockKey 락 키
     * @param timeoutMs 타임아웃 (밀리초)
     * @return 락 값 (획득 실패 시 null)
     */
    String tryAcquire(String lockKey, long timeoutMs);

    /**
     * 락 해제
     *
     * @param lockKey 락 키
     * @param lockValue 락 값
     */
    void release(String lockKey, String lockValue);
}
