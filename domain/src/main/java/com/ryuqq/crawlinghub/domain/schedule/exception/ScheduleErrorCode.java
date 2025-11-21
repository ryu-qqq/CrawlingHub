package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;

/** Schedule Bounded Context에서 사용하는 ErrorCode Enum입니다. */
public enum ScheduleErrorCode implements ErrorCode {
    CRAWL_SCHEDULER_NOT_FOUND("SCHEDULE-001", 404, "존재하지 않는 크롤 스케줄러입니다."),
    DUPLICATE_SCHEDULER_NAME("SCHEDULE-002", 409, "이미 존재하는 스케줄러 이름입니다.");

    private final String code;
    private final int httpStatus;
    private final String message;

    ScheduleErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
