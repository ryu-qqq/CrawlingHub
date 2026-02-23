package com.ryuqq.crawlinghub.domain.execution.exception;

import java.util.Map;

/**
 * 존재하지 않는 CrawlExecution 조회 시 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlExecutionNotFoundException extends CrawlExecutionException {

    private static final CrawlExecutionErrorCode ERROR_CODE =
            CrawlExecutionErrorCode.CRAWL_EXECUTION_NOT_FOUND;

    /**
     * CrawlExecution ID로 예외 생성
     *
     * @param crawlExecutionId 조회 시도한 CrawlExecution ID
     */
    public CrawlExecutionNotFoundException(Long crawlExecutionId) {
        super(
                ERROR_CODE,
                String.format("존재하지 않는 크롤 실행입니다. ID: %d", crawlExecutionId),
                Map.of("crawlExecutionId", crawlExecutionId));
    }
}
