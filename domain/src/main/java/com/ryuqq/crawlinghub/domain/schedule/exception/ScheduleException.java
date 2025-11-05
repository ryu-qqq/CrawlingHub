package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

/**
 * Schedule Exception - Sealed Abstract Class
 *
 * <p>Schedule Bounded Context의 모든 예외를 묶는 Sealed 추상 클래스입니다.</p>
 *
 * <p><strong>예외 계층:</strong></p>
 * <ul>
 *   <li>PlaceholderScheduleException - 임시 예외 (Sealed 제약 충족용)</li>
 *   <li>향후 Schedule 관련 예외가 추가될 때 여기에 정의됩니다.</li>
 * </ul>
 *
 * <p><strong>Sealed Classes 장점:</strong></p>
 * <ul>
 *   <li>✅ 허용된 예외만 상속 가능 (컴파일 타임 검증)</li>
 *   <li>✅ Switch Expression에서 Exhaustive Checking (모든 케이스 처리 강제)</li>
 *   <li>✅ 타입 안전성 향상</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public sealed abstract class ScheduleException extends DomainException
    permits PlaceholderScheduleException {

    /**
     * ScheduleException 생성자
     *
     * @param message 에러 메시지
     */
    protected ScheduleException(String message) {
        super(message);
    }

    /**
     * ScheduleException 생성자 (원인 포함)
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    protected ScheduleException(String message, Throwable cause) {
        super(message, cause);
    }
}

