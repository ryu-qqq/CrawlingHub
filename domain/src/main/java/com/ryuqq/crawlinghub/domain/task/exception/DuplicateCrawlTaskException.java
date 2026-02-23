package com.ryuqq.crawlinghub.domain.task.exception;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.Map;

/**
 * 중복된 CrawlTask 생성 시 발생하는 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public final class DuplicateCrawlTaskException extends CrawlTaskException {

    private static final CrawlTaskErrorCode ERROR_CODE = CrawlTaskErrorCode.DUPLICATE_CRAWL_TASK;

    /**
     * 셀러 ID와 태스크 유형으로 예외 생성
     *
     * @param sellerId 셀러 ID
     * @param taskType 태스크 유형
     */
    public DuplicateCrawlTaskException(Long sellerId, CrawlTaskType taskType) {
        super(
                ERROR_CODE,
                String.format("중복된 크롤 태스크입니다. 셀러: %d, 유형: %s", sellerId, taskType),
                Map.of("sellerId", sellerId, "taskType", taskType.name()));
    }
}
