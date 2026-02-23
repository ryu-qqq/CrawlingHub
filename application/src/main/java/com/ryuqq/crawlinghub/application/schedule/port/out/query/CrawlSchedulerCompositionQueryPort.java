package com.ryuqq.crawlinghub.application.schedule.port.out.query;

import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import java.util.Optional;

/**
 * CrawlScheduler Composite 조회 Port
 *
 * <p>스케줄러 상세 조회를 위한 크로스 도메인 Composite 쿼리 포트입니다. Scheduler + Seller + Task 정보를 한 번에 조회합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlSchedulerCompositionQueryPort {

    /**
     * 스케줄러 상세 정보 조회 (Composite)
     *
     * @param crawlSchedulerId 크롤 스케줄러 ID
     * @return 스케줄러 상세 결과 (Optional)
     */
    Optional<CrawlSchedulerDetailResult> findSchedulerDetailById(Long crawlSchedulerId);
}
