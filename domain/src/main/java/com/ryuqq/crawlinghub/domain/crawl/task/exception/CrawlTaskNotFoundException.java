package com.ryuqq.crawlinghub.domain.crawl.task.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;

import java.util.Map;

/**
 * 존재하지 않는 CrawlTask 조회 시 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlTaskNotFoundException extends DomainException {

    private static final CrawlTaskErrorCode ERROR_CODE = CrawlTaskErrorCode.CRAWL_TASK_NOT_FOUND;

    /**
     * CrawlTask ID로 예외 생성
     *
     * @param crawlTaskId 조회 시도한 CrawlTask ID
     */
    public CrawlTaskNotFoundException(Long crawlTaskId) {
        super(
                ERROR_CODE.getCode(),
                ERROR_CODE.getMessage(),
                Map.of("crawlTaskId", crawlTaskId)
        );
    }
}
