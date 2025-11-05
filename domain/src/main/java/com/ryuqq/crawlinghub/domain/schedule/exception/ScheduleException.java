package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

/**
 * Schedule Exception - Sealed Interface
 *
 * <p>Schedule Bounded Context의 모든 예외를 묶는 Sealed 인터페이스입니다.</p>
 * <p>컴파일 타임에 허용된 예외만 정의할 수 있어 타입 안전성을 보장합니다.</p>
 *
 * <p><strong>예외 계층:</strong></p>
 * <ul>
 *   <li>향후 Schedule 관련 예외가 추가될 때 여기에 정의됩니다.</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public sealed interface ScheduleException extends DomainException
    permits PlaceholderScheduleException {
}

