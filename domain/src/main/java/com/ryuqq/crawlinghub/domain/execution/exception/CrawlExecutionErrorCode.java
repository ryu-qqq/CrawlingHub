package com.ryuqq.crawlinghub.domain.execution.exception;

import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;

/**
 * CrawlExecution Bounded Context 에러 코드 Enum
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CrawlExecutionErrorCode implements ErrorCode {

    /** 크롤 실행을 찾을 수 없음 */
    CRAWL_EXECUTION_NOT_FOUND("CRAWL-EXEC-001", 404, "존재하지 않는 크롤 실행 이력입니다."),

    /** 유효하지 않은 상태 전환 */
    INVALID_CRAWL_EXECUTION_STATE(
            "CRAWL-EXEC-002", 400, "유효하지 않은 상태 전환입니다. RUNNING 상태에서만 완료 처리가 가능합니다.");

    private final String code;
    private final int httpStatus;
    private final String message;

    CrawlExecutionErrorCode(String code, int httpStatus, String message) {
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
