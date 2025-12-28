package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.SellerStatistics;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.factory.query.SellerQueryFactory;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.application.seller.port.in.query.SearchSellersUseCase;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Search Sellers Service
 *
 * <p>셀러 목록 조회 UseCase 구현
 *
 * <ul>
 *   <li>조회 전용
 *   <li>Query DTO → Criteria 변환 (QueryFactory)
 *   <li>Domain → SummaryResponse 변환 (Assembler)
 * </ul>
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchSellersService implements SearchSellersUseCase {

    private final SellerReadManager readManager;
    private final SellerQueryFactory queryFactory;
    private final SellerAssembler assembler;
    private final CrawlSchedulerReadManager schedulerReadManager;
    private final CrawlTaskReadManager taskReadManager;
    private final CrawledProductReadManager productReadManager;

    public SearchSellersService(
            SellerReadManager readManager,
            SellerQueryFactory queryFactory,
            SellerAssembler assembler,
            CrawlSchedulerReadManager schedulerReadManager,
            CrawlTaskReadManager taskReadManager,
            CrawledProductReadManager productReadManager) {
        this.readManager = readManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
        this.schedulerReadManager = schedulerReadManager;
        this.taskReadManager = taskReadManager;
        this.productReadManager = productReadManager;
    }

    @Override
    public PageResponse<SellerSummaryResponse> execute(SearchSellersQuery query) {
        // 1. Query → Criteria 변환 (QueryFactory)
        SellerQueryCriteria criteria = queryFactory.createCriteria(query);

        // 2. 조회
        List<Seller> sellers = readManager.findByCriteria(criteria);
        long totalElements = readManager.countByCriteria(criteria);

        // 3. 셀러별 통계 정보 조회
        Map<SellerId, SellerStatistics> statisticsMap = buildStatisticsMap(sellers);

        // 4. Domain → PageResponse 변환 (Assembler)
        return assembler.toPageResponse(
                sellers, statisticsMap, criteria.page(), criteria.size(), totalElements);
    }

    /**
     * 셀러 목록에 대한 통계 정보 조회
     *
     * @param sellers 셀러 목록
     * @return 셀러 ID별 통계 맵
     */
    private Map<SellerId, SellerStatistics> buildStatisticsMap(List<Seller> sellers) {
        Map<SellerId, SellerStatistics> statisticsMap = new HashMap<>();

        for (Seller seller : sellers) {
            SellerId sellerId = seller.getSellerId();

            // 스케줄러 통계
            int activeSchedulerCount =
                    (int) schedulerReadManager.countActiveSchedulersBySellerId(sellerId);
            int totalSchedulerCount = (int) schedulerReadManager.countBySellerId(sellerId);

            // 최근 태스크 정보
            Optional<CrawlTask> latestTask = taskReadManager.findLatestBySellerId(sellerId);
            String lastTaskStatus = latestTask.map(task -> task.getStatus().name()).orElse(null);
            java.time.Instant lastTaskExecutedAt =
                    latestTask.map(CrawlTask::getCreatedAt).orElse(null);

            // 상품 통계
            long totalProductCount = productReadManager.countBySellerId(sellerId);

            SellerStatistics statistics =
                    new SellerStatistics(
                            activeSchedulerCount,
                            totalSchedulerCount,
                            lastTaskStatus,
                            lastTaskExecutedAt,
                            totalProductCount);

            statisticsMap.put(sellerId, statistics);
        }

        return statisticsMap;
    }
}
