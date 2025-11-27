package com.ryuqq.crawlinghub.domain.task.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.Map;

/**
 * 유효하지 않은 CrawlTask 상태 전환 시 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public final class InvalidCrawlTaskStateException extends DomainException {

    private static final CrawlTaskErrorCode ERROR_CODE =
            CrawlTaskErrorCode.INVALID_CRAWL_TASK_STATE;

    /**
     * 현재 상태와 기대 상태로 예외 생성
     *
     * @param currentStatus 현재 상태
     * @param expectedStatus 기대 상태
     */
    public InvalidCrawlTaskStateException(
            CrawlTaskStatus currentStatus, CrawlTaskStatus expectedStatus) {
        super(
                ERROR_CODE.getCode(),
                "유효하지 않은 상태 전환입니다. 현재 상태: " + currentStatus + ", 기대 상태: " + expectedStatus,
                Map.of(
                        "currentStatus", currentStatus.name(),
                        "expectedStatus", expectedStatus.name()));
    }
}
