package com.ryuqq.crawlinghub.domain.mustit.seller.outbox;

/**
 * Write-Ahead Log 상태
 * <p>
 * Transactional Outbox Pattern에서 사용되는 WAL 상태를 정의합니다.
 * Orchestrator SDK의 WriteAheadState와 매핑됩니다.
 * </p>
 * <p>
 * WAL (Write-Ahead Log):
 * <ul>
 *   <li>Executor 실행 전에 Outcome을 미리 기록</li>
 *   <li>장애 발생 시 Finalizer가 WAL을 읽어서 복구</li>
 *   <li>PENDING: WAL 기록 완료, Executor 실행 대기 중</li>
 *   <li>COMPLETED: Executor 실행 완료, WAL 처리 완료</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public enum WriteAheadState {
    /**
     * WAL 기록 대기 또는 기록 완료 (Executor 실행 전/중)
     */
    PENDING,

    /**
     * WAL 기록 완료 (Executor 실행 완료 후 Finalizer 처리 완료)
     */
    COMPLETED
}
