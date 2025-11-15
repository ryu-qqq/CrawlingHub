package com.ryuqq.crawlinghub.domain.vo;

/**
 * ProductOutbox 상태 Enum
 *
 * <p>ProductOutbox의 전송 상태를 표현합니다.</p>
 *
 * <p>상태 정의:</p>
 * <ul>
 *   <li>{@link #WAITING} - 대기 중 (전송 대기)</li>
 *   <li>{@link #SENDING} - 전송 중 (외부 시스템으로 전송 진행)</li>
 *   <li>{@link #COMPLETED} - 완료 (전송 성공)</li>
 *   <li>{@link #FAILED} - 실패 (전송 실패, 재시도 가능)</li>
 * </ul>
 *
 * <p>상태 전이 흐름:</p>
 * <pre>
 * WAITING → SENDING → COMPLETED
 *                  ↓
 *                FAILED → (재시도) → WAITING
 * </pre>
 */
public enum OutboxStatus {

    /**
     * 대기 중 - 전송 대기
     */
    WAITING,

    /**
     * 전송 중 - 외부 시스템으로 전송 진행
     */
    SENDING,

    /**
     * 완료 - 전송 성공
     */
    COMPLETED,

    /**
     * 실패 - 전송 실패, 재시도 가능
     */
    FAILED
}
