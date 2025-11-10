package com.ryuqq.crawlinghub.application.task.port.out;

import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageOutbox;
import com.ryuqq.crawlinghub.domain.task.outbox.TaskMessageStatus;

import java.util.List;
import java.util.Optional;

/**
 * Task 메시지 Outbox 조회 Port
 *
 * <p>Persistence Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface LoadTaskMessageOutboxPort {

    /**
     * Outbox ID로 조회
     *
     * @param outboxId Outbox ID
     * @return TaskMessageOutbox (없으면 Optional.empty())
     */
    Optional<TaskMessageOutbox> findById(Long outboxId);

    /**
     * 상태별 조회
     *
     * @param status 메시지 상태 (PENDING, SENT)
     * @return 해당 상태의 Outbox 목록
     */
    List<TaskMessageOutbox> findByStatus(TaskMessageStatus status);

    /**
     * 상태별 조회 (비관적 락)
     *
     * <p>TaskMessageScheduler가 동시 실행될 때 중복 처리 방지
     *
     * <p>QueryDSL 구현 예시:
     * <pre>
     * query.selectFrom(qTaskMessageOutbox)
     *      .where(qTaskMessageOutbox.status.eq(status))
     *      .setLockMode(LockModeType.PESSIMISTIC_WRITE)
     *      .fetch();
     * </pre>
     *
     * @param status 메시지 상태 (PENDING, SENT)
     * @return 해당 상태의 Outbox 목록 (비관적 락 적용)
     */
    List<TaskMessageOutbox> findByStatusWithLock(TaskMessageStatus status);
}
