package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * SchedulerOutbox 상태 Enum
 *
 * <p>SchedulerOutbox의 전송 상태를 나타냅니다.</p>
 *
 * <p><strong>상태 전환 흐름:</strong></p>
 * <pre>
 * WAITING (대기)
 *    ↓
 * SENT (전송 성공) 또는 FAILED (전송 실패)
 *    ↓ (FAILED인 경우 재시도)
 * SENT (최종 성공)
 * </pre>
 *
 * <p><strong>각 상태의 의미:</strong></p>
 * <ul>
 *   <li>✅ WAITING: 전송 대기 중 (생성 직후)</li>
 *   <li>✅ SENT: 전송 성공 (EventBridge에 전달 완료)</li>
 *   <li>✅ FAILED: 전송 실패 (재시도 필요)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public enum SchedulerOutboxStatus {

    /**
     * 전송 대기 중 (생성 직후)
     */
    WAITING,

    /**
     * 전송 성공 (EventBridge에 전달 완료)
     */
    SENT,

    /**
     * 전송 실패 (재시도 필요)
     */
    FAILED;

    /**
     * String 값으로부터 SchedulerOutboxStatus 생성
     *
     * @param value 문자열 값
     * @return SchedulerOutboxStatus enum
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutboxStatus of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("SchedulerOutboxStatus cannot be null");
        }
        return valueOf(value.toUpperCase());
    }
}
