package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.application.seller.port.in.query.GetSellerUseCase;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 셀러 단건 조회 UseCase 구현체
 *
 * <p>셀러 기본 정보와 함께 연관 스케줄러, 최근 태스크를 조회하여 반환
 */
@Service
public class GetSellerService implements GetSellerUseCase {

    private static final int RECENT_TASKS_LIMIT = 5;

    private final SellerReadManager sellerReadManager;
    private final CrawlSchedulerReadManager crawlSchedulerReadManager;
    private final CrawlTaskReadManager crawlTaskReadManager;
    private final SellerAssembler sellerAssembler;

    public GetSellerService(
            SellerReadManager sellerReadManager,
            CrawlSchedulerReadManager crawlSchedulerReadManager,
            CrawlTaskReadManager crawlTaskReadManager,
            SellerAssembler sellerAssembler) {
        this.sellerReadManager = sellerReadManager;
        this.crawlSchedulerReadManager = crawlSchedulerReadManager;
        this.crawlTaskReadManager = crawlTaskReadManager;
        this.sellerAssembler = sellerAssembler;
    }

    @Override
    public SellerDetailResponse execute(GetSellerQuery query) {
        SellerId sellerId = SellerId.of(query.sellerId());

        Seller seller =
                sellerReadManager
                        .findById(sellerId)
                        .orElseThrow(() -> new SellerNotFoundException(query.sellerId()));

        List<CrawlScheduler> schedulers = crawlSchedulerReadManager.findBySellerId(sellerId);
        List<CrawlTask> recentTasks =
                crawlTaskReadManager.findRecentBySellerId(sellerId, RECENT_TASKS_LIMIT);

        return sellerAssembler.toDetailResponse(seller, schedulers, recentTasks);
    }
}
