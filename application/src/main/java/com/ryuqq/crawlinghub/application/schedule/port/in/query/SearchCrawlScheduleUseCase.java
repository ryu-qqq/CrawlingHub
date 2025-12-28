package com.ryuqq.crawlinghub.application.schedule.port.in.query;

import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerDetailResponse;

/**
 * 단건 스케줄 조회 UseCase.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchCrawlScheduleUseCase {

    /**
     * 크롤 스케줄러 단건 상세 조회
     *
     * @param crawlSchedulerId 크롤 스케줄러 ID
     * @return 스케줄러 상세 응답
     */
    CrawlSchedulerDetailResponse execute(Long crawlSchedulerId);
}
