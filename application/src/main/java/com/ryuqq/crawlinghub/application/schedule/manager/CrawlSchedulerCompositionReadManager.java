package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerCompositionQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlScheduler Composite 조회 ReadManager
 *
 * <p><strong>책임</strong>: Composite 패턴을 통한 스케줄러 상세 조회
 *
 * <p><strong>규칙</strong>: CrawlSchedulerCompositionQueryPort 단일 의존, 읽기 전용 트랜잭션
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerCompositionReadManager {

    private final CrawlSchedulerCompositionQueryPort compositionQueryPort;

    public CrawlSchedulerCompositionReadManager(
            CrawlSchedulerCompositionQueryPort compositionQueryPort) {
        this.compositionQueryPort = compositionQueryPort;
    }

    /**
     * 스케줄러 상세 정보 조회
     *
     * @param crawlSchedulerId 크롤 스케줄러 ID
     * @return 스케줄러 상세 결과
     * @throws CrawlSchedulerNotFoundException 스케줄러가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public CrawlSchedulerDetailResult getSchedulerDetail(Long crawlSchedulerId) {
        return compositionQueryPort
                .findSchedulerDetailById(crawlSchedulerId)
                .orElseThrow(() -> new CrawlSchedulerNotFoundException(crawlSchedulerId));
    }
}
