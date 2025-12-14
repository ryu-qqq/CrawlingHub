package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 이미 등록된 스케줄러 이름이 존재할 때 발생하는 예외입니다. */
public final class DuplicateSchedulerNameException extends DomainException {

    private static final ScheduleErrorCode ERROR_CODE = ScheduleErrorCode.DUPLICATE_SCHEDULER_NAME;

    public DuplicateSchedulerNameException(Long sellerId, String schedulerName) {
        super(
                ERROR_CODE,
                String.format("이미 존재하는 스케줄러 이름입니다. 셀러: %d, 이름: %s", sellerId, schedulerName),
                Map.of("sellerId", sellerId, "schedulerName", schedulerName));
    }
}
