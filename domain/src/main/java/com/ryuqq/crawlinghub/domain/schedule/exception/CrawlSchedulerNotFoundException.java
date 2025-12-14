package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 존재하지 않는 크롤 스케줄러를 조회했을 때 발생하는 예외입니다. */
public final class CrawlSchedulerNotFoundException extends DomainException {

    private static final ScheduleErrorCode ERROR_CODE = ScheduleErrorCode.CRAWL_SCHEDULER_NOT_FOUND;

    public CrawlSchedulerNotFoundException(long crawlSchedulerId) {
        super(
                ERROR_CODE,
                String.format("존재하지 않는 크롤 스케줄러입니다. ID: %d", crawlSchedulerId),
                Map.of("crawlSchedulerId", crawlSchedulerId));
    }
}
