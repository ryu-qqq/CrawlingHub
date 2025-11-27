package com.ryuqq.crawlinghub.application.common.component.lock;

import com.ryuqq.crawlinghub.application.common.port.out.lock.DistributedLockPort;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 분산 락 실행기
 *
 * <p><strong>용도</strong>: 분산 환경에서 중복 처리 방지를 위한 락 기반 작업 실행
 *
 * <p><strong>사용 시나리오</strong>:
 *
 * <ul>
 *   <li>EventBridge 트리거 중복 방지 (여러 워커가 동시에 폴링)
 *   <li>CrawlTask 중복 실행 방지
 * </ul>
 *
 * <p><strong>동작 방식</strong>:
 *
 * <pre>
 * 락 획득 성공 → action 실행 → 결과 반환 → 락 해제
 * 락 획득 실패 → Optional.empty() 반환 (skip)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class DistributedLockExecutor {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockExecutor.class);

    private final DistributedLockPort distributedLockPort;

    public DistributedLockExecutor(DistributedLockPort distributedLockPort) {
        this.distributedLockPort = distributedLockPort;
    }

    /**
     * 락을 획득하고 작업 실행 (결과 반환)
     *
     * <p>락 획득 실패 시 Optional.empty() 반환 (예외 없이 skip)
     *
     * @param lockType 락 타입
     * @param identifier 식별자 (schedulerId, taskId 등)
     * @param action 락 획득 후 실행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과 (락 획득 실패 시 empty)
     */
    public <T> Optional<T> tryExecuteWithLock(
            LockType lockType, Object identifier, Supplier<T> action) {
        String lockKey = lockType.buildKey(identifier);

        log.debug("Attempting to acquire lock: type={}, key={}", lockType, lockKey);

        T result =
                distributedLockPort.executeWithLock(
                        lockKey,
                        lockType.getDefaultWaitTimeMs(),
                        lockType.getDefaultLeaseTimeMs(),
                        TimeUnit.MILLISECONDS,
                        action);

        if (result == null) {
            log.info(
                    "Lock acquisition skipped (already held by another worker): type={}, key={}",
                    lockType,
                    lockKey);
            return Optional.empty();
        }

        log.debug("Lock execution completed: type={}, key={}", lockType, lockKey);
        return Optional.of(result);
    }

    /**
     * 락을 획득하고 작업 실행 (반환값 없음)
     *
     * <p>락 획득 실패 시 false 반환 (예외 없이 skip)
     *
     * @param lockType 락 타입
     * @param identifier 식별자 (schedulerId, taskId 등)
     * @param action 락 획득 후 실행할 작업
     * @return 작업 실행 여부 (락 획득 실패 시 false)
     */
    public boolean tryExecuteWithLock(LockType lockType, Object identifier, Runnable action) {
        String lockKey = lockType.buildKey(identifier);

        log.debug("Attempting to acquire lock: type={}, key={}", lockType, lockKey);

        boolean executed =
                distributedLockPort.executeWithLock(
                        lockKey,
                        lockType.getDefaultWaitTimeMs(),
                        lockType.getDefaultLeaseTimeMs(),
                        TimeUnit.MILLISECONDS,
                        action);

        if (!executed) {
            log.info(
                    "Lock acquisition skipped (already held by another worker): type={}, key={}",
                    lockType,
                    lockKey);
        } else {
            log.debug("Lock execution completed: type={}, key={}", lockType, lockKey);
        }

        return executed;
    }

    /**
     * 락을 획득하고 작업 실행 (커스텀 타임아웃)
     *
     * @param lockType 락 타입
     * @param identifier 식별자
     * @param waitTimeMs 락 대기 시간 (ms)
     * @param leaseTimeMs 락 유지 시간 (ms)
     * @param action 락 획득 후 실행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과 (락 획득 실패 시 empty)
     */
    public <T> Optional<T> tryExecuteWithLock(
            LockType lockType,
            Object identifier,
            long waitTimeMs,
            long leaseTimeMs,
            Supplier<T> action) {
        String lockKey = lockType.buildKey(identifier);

        log.debug(
                "Attempting to acquire lock with custom timeout: type={}, key={}, waitTime={}ms,"
                        + " leaseTime={}ms",
                lockType,
                lockKey,
                waitTimeMs,
                leaseTimeMs);

        T result =
                distributedLockPort.executeWithLock(
                        lockKey, waitTimeMs, leaseTimeMs, TimeUnit.MILLISECONDS, action);

        if (result == null) {
            log.info(
                    "Lock acquisition skipped (already held by another worker): type={}, key={}",
                    lockType,
                    lockKey);
            return Optional.empty();
        }

        log.debug("Lock execution completed: type={}, key={}", lockType, lockKey);
        return Optional.of(result);
    }

    /**
     * 현재 락이 보유 중인지 확인
     *
     * @param lockType 락 타입
     * @param identifier 식별자
     * @return 락 보유 여부
     */
    public boolean isLocked(LockType lockType, Object identifier) {
        String lockKey = lockType.buildKey(identifier);
        return distributedLockPort.isLocked(lockKey);
    }
}
