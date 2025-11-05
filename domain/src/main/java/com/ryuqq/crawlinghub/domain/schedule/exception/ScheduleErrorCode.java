package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.ErrorCode;

/**
 * Schedule Bounded Context 전용 ErrorCode
 *
 * <p>Schedule 도메인에서 발생하는 모든 비즈니스 예외의 에러 코드를 정의합니다.</p>
 *
 * <p><strong>코드 체계:</strong></p>
 * <ul>
 *   <li>SCHEDULE-001 ~ SCHEDULE-009: Not Found (404)</li>
 *   <li>SCHEDULE-010 ~ SCHEDULE-099: Conflict (409)</li>
 *   <li>SCHEDULE-101 ~ SCHEDULE-199: Bad Request (400)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public enum ScheduleErrorCode implements ErrorCode {

    /**
     * Placeholder 예외 (임시)
     */
    SCHEDULE_PLACEHOLDER("SCHEDULE-001", 500, "Placeholder exception", "Schedule Placeholder");

    private final String code;
    private final int httpStatus;
    private final String message;
    private final String title;

    ScheduleErrorCode(String code, int httpStatus, String message, String title) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
        this.title = title;
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

    /**
     * HTTP 응답용 title 반환
     *
     * @return title (예: "Schedule Placeholder")
     */
    public String getTitle() {
        return title;
    }
}

