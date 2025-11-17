package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * ExecutionStatus - 스케줄 실행 상태 Enum
 *
 * <p>CrawlingScheduleExecution의 실행 상태를 나타냅니다.</p>
 *
 * <p><strong>상태 전환 흐름:</strong></p>
 * <pre>
 * PENDING (생성 직후)
 *    ↓
 * RUNNING (실행 시작)
 *    ↓
 * COMPLETED (정상 완료) 또는 FAILED (실패)
 * </pre>
 *
 * <p><strong>각 상태의 의미:</strong></p>
 * <ul>
 *   <li>✅ PENDING: 실행 대기 중 (생성 직후)</li>
 *   <li>✅ RUNNING: 실행 중 (크롤링 작업 진행 중)</li>
 *   <li>✅ COMPLETED: 실행 완료 (모든 작업 성공)</li>
 *   <li>✅ FAILED: 실행 실패 (오류 발생)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public enum ExecutionStatus {
    /**
     * 실행 대기 중 (생성 직후 초기 상태)
     */
    PENDING,

    /**
     * 실행 중 (크롤링 작업 진행 중)
     */
    RUNNING,

    /**
     * 실행 완료 (모든 작업 정상 완료)
     */
    COMPLETED,

    /**
     * 실행 실패 (오류 발생으로 실행 중단)
     */
    FAILED
}
