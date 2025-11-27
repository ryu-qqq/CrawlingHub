package com.ryuqq.crawlinghub.domain.task.exception;

import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;

/**
 * CrawlTask Bounded Context 에러 코드 Enum
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CrawlTaskErrorCode implements ErrorCode {

    /** 크롤 태스크를 찾을 수 없음 */
    CRAWL_TASK_NOT_FOUND("CRAWL-TASK-001", 404, "존재하지 않는 크롤 태스크입니다."),

    /** 유효하지 않은 상태 전환 */
    INVALID_CRAWL_TASK_STATE("CRAWL-TASK-002", 400, "유효하지 않은 상태 전환입니다."),

    /** 중복 크롤 태스크 */
    DUPLICATE_CRAWL_TASK("CRAWL-TASK-003", 409, "이미 존재하는 크롤 태스크입니다."),

    /** 재시도 횟수 초과 */
    RETRY_LIMIT_EXCEEDED("CRAWL-TASK-004", 400, "재시도 횟수를 초과했습니다."),

    /** 태스크 실행 실패 */
    CRAWL_TASK_EXECUTION_FAILED("CRAWL-TASK-005", 500, "크롤 태스크 실행에 실패했습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;

    CrawlTaskErrorCode(String code, int httpStatus, String message) {
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
