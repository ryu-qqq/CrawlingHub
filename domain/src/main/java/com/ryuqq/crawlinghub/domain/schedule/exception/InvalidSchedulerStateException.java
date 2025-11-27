package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.util.Map;

/**
 * 유효하지 않은 스케줄러 상태 예외
 *
 * <p>스케줄러 상태 전환이 유효하지 않을 때 발생
 *
 * @author development-team
 * @since 1.0.0
 */
public final class InvalidSchedulerStateException extends DomainException {

    private static final ScheduleErrorCode ERROR_CODE = ScheduleErrorCode.INVALID_SCHEDULER_STATE;

    /**
     * 현재 상태와 기대 상태로 예외 생성
     *
     * @param currentStatus 현재 상태
     * @param expectedStatus 기대 상태
     */
    public InvalidSchedulerStateException(
            SchedulerStatus currentStatus, SchedulerStatus expectedStatus) {
        super(
                ERROR_CODE.getCode(),
                "스케줄러 상태가 유효하지 않습니다. 현재: " + currentStatus + ", 기대: " + expectedStatus,
                Map.of(
                        "currentStatus", currentStatus.name(),
                        "expectedStatus", expectedStatus.name()));
    }
}
