package com.ryuqq.crawlinghub.domain.execution.exception;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.util.Map;

/**
 * 유효하지 않은 CrawlExecution 상태 전환 시 발생하는 예외
 *
 * <p>CrawlExecution은 RUNNING 상태에서만 완료 처리가 가능합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public final class InvalidCrawlExecutionStateException extends CrawlExecutionException {

    private static final CrawlExecutionErrorCode ERROR_CODE =
            CrawlExecutionErrorCode.INVALID_CRAWL_EXECUTION_STATE;

    /**
     * 현재 상태로 예외 생성
     *
     * @param currentStatus 현재 상태
     */
    public InvalidCrawlExecutionStateException(CrawlExecutionStatus currentStatus) {
        super(
                ERROR_CODE,
                String.format(
                        "유효하지 않은 상태 전환입니다. 현재 상태: %s (RUNNING 상태에서만 완료 처리 가능)", currentStatus),
                Map.of(
                        "currentStatus", currentStatus.name(),
                        "expectedStatus", CrawlExecutionStatus.RUNNING.name()));
    }
}
