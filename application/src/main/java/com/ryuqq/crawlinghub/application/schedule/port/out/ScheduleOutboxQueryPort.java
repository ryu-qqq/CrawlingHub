package com.ryuqq.crawlinghub.application.schedule.port.out;

import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;

import java.util.List;
import java.util.Optional;

/**
 * Schedule Outbox Query Port (CQRS - Read)
 *
 * <p>ScheduleOutbox의 Read 작업을 담당하는 Port입니다.
 * <p>CQRS 패턴에 따라 Query(읽기) 작업만 포함합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface ScheduleOutboxQueryPort {

    /**
     * Idempotency Key로 Outbox 조회
     *
     * @param idemKey Idempotency Key
     * @return Outbox (Optional)
     */
    Optional<ScheduleOutbox> findByIdemKey(String idemKey);

    /**
     * Idempotency Key 존재 여부 확인
     *
     * @param idemKey Idempotency Key
     * @return 존재 여부
     */
    boolean existsByIdemKey(String idemKey);

    /**
     * WAL State가 PENDING인 Outbox 목록 조회 (생성 시간 오름차순)
     *
     * <p>S2 Phase (Execute)에서 처리할 대상을 조회합니다.
     *
     * @return PENDING 상태의 Outbox 목록
     */
    List<ScheduleOutbox> findByWalStatePending();

    /**
     * Operation State가 FAILED인 Outbox 목록 조회
     *
     * <p>S3 Phase (Finalize)에서 재시도 대상을 조회합니다.
     *
     * @return FAILED 상태의 Outbox 목록
     */
    List<ScheduleOutbox> findByOperationStateFailed();

    /**
     * WAL State가 COMPLETED인 Outbox 목록 조회
     *
     * <p>S3 Phase (Finalize)에서 정리 대상을 조회합니다.
     *
     * @return COMPLETED 상태의 Outbox 목록
     */
    List<ScheduleOutbox> findByWalStateCompleted();
}
