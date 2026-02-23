package com.ryuqq.crawlinghub.application.schedule.service.query;

import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerCompositionReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlScheduleUseCase;
import org.springframework.stereotype.Service;

/**
 * 크롤 스케줄러 단건 상세 조회 서비스
 *
 * <p><strong>책임</strong>: Composite ReadManager를 통한 스케줄러 상세 정보 조회
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetCrawlSchedulerService implements SearchCrawlScheduleUseCase {

    private final CrawlSchedulerCompositionReadManager compositionReadManager;

    public GetCrawlSchedulerService(CrawlSchedulerCompositionReadManager compositionReadManager) {
        this.compositionReadManager = compositionReadManager;
    }

    @Override
    public CrawlSchedulerDetailResult execute(Long crawlSchedulerId) {
        return compositionReadManager.getSchedulerDetail(crawlSchedulerId);
    }
}
