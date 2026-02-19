package com.ryuqq.crawlinghub.application.schedule.service.query;

import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlScheduleUseCase;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 크롤 스케줄러 단건 상세 조회 서비스
 *
 * <p><strong>책임</strong>: 스케줄러 상세 정보 조회 및 조립
 *
 * <p><strong>조회 항목</strong>:
 *
 * <ul>
 *   <li>스케줄러 기본 정보
 *   <li>연관 셀러 정보
 *   <li>실행 정보 (다음 실행 시각, 마지막 실행)
 *   <li>통계 정보 (총 태스크, 성공률 등)
 *   <li>최근 태스크 목록 (최대 10개)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetCrawlSchedulerService implements SearchCrawlScheduleUseCase {

    private static final int RECENT_TASKS_LIMIT = 10;

    private final CrawlSchedulerReadManager crawlSchedulerReadManager;
    private final SellerReadManager sellerReadManager;
    private final CrawlTaskReadManager crawlTaskReadManager;
    private final CrawlSchedulerAssembler crawlSchedulerAssembler;

    public GetCrawlSchedulerService(
            CrawlSchedulerReadManager crawlSchedulerReadManager,
            SellerReadManager sellerReadManager,
            CrawlTaskReadManager crawlTaskReadManager,
            CrawlSchedulerAssembler crawlSchedulerAssembler) {
        this.crawlSchedulerReadManager = crawlSchedulerReadManager;
        this.sellerReadManager = sellerReadManager;
        this.crawlTaskReadManager = crawlTaskReadManager;
        this.crawlSchedulerAssembler = crawlSchedulerAssembler;
    }

    @Override
    public CrawlSchedulerDetailResponse execute(Long crawlSchedulerId) {
        CrawlSchedulerId schedulerId = CrawlSchedulerId.of(crawlSchedulerId);

        CrawlScheduler scheduler =
                crawlSchedulerReadManager
                        .findById(schedulerId)
                        .orElseThrow(
                                () -> new CrawlSchedulerNotFoundException(schedulerId.value()));

        Seller seller = findSellerOrNull(scheduler.getSellerId());
        List<CrawlTask> recentTasks = findRecentTasks(schedulerId);
        Map<CrawlTaskStatus, Long> statusCounts = countTasksByStatus(schedulerId);

        return crawlSchedulerAssembler.toDetailResponse(
                scheduler, seller, recentTasks, statusCounts);
    }

    private Seller findSellerOrNull(SellerId sellerId) {
        return sellerReadManager.findById(sellerId).orElse(null);
    }

    private List<CrawlTask> findRecentTasks(CrawlSchedulerId schedulerId) {
        CrawlTaskCriteria criteria =
                new CrawlTaskCriteria(
                        schedulerId, null, null, null, null, null, 0, RECENT_TASKS_LIMIT);
        return crawlTaskReadManager.findByCriteria(criteria);
    }

    private Map<CrawlTaskStatus, Long> countTasksByStatus(CrawlSchedulerId schedulerId) {
        CrawlTaskStatisticsCriteria statisticsCriteria =
                new CrawlTaskStatisticsCriteria(schedulerId, null, null, null);
        return crawlTaskReadManager.countByStatus(statisticsCriteria);
    }
}
