package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

/**
 * Schedule Exception - Abstract Class
 *
 * <p>Schedule Bounded Context의 모든 예외를 묶는 추상 클래스입니다.</p>
 *
 * <p><strong>예외 계층:</strong></p>
 * <ul>
 *   <li>향후 Schedule 관련 예외가 추가될 때 여기에 정의됩니다.</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public abstract class ScheduleException extends DomainException {

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

