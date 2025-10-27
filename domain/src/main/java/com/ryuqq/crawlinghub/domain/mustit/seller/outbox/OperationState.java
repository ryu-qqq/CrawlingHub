package com.ryuqq.crawlinghub.domain.mustit.seller.outbox;

/**
 * Orchestrator 작업 상태
 * <p>
 * Transactional Outbox Pattern에서 사용되는 작업 상태를 정의합니다.
 * Orchestrator SDK의 OperationState와 매핑됩니다.
 * </p>
 * <p>
 * 상태 전이:
 * <ul>
 *   <li>PENDING → IN_PROGRESS: Orchestrator 시작</li>
 *   <li>IN_PROGRESS → COMPLETED: 정상 완료</li>
 *   <li>IN_PROGRESS → FAILED: 실패 (재시도 가능)</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public enum OperationState {
    /**
     * 대기 중 - Outbox 저장 직후 초기 상태
     */
    PENDING,

    /**
     * 실행 중 - Orchestrator가 Executor를 실행 중
     */
    IN_PROGRESS,

    /**
     * 완료 - Executor 실행 성공
     */
    COMPLETED,

    /**
     * 실패 - Executor 실행 실패 (재시도 가능)
     */
    FAILED
}
