package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 이미 등록된 스케줄러 이름이 존재할 때 발생하는 예외입니다. */
public final class DuplicateSchedulerNameException extends DomainException {

    private static final ScheduleErrorCode ERROR_CODE = ScheduleErrorCode.DUPLICATE_SCHEDULER_NAME;

    private final Map<String, Object> args;

    public DuplicateSchedulerNameException(Long sellerId, String schedulerName) {
        super(ERROR_CODE.getMessage());
        this.args = Map.of("sellerId", sellerId, "schedulerName", schedulerName);
    }

    @Override
    public String code() {
        return ERROR_CODE.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return args;
    }
}
