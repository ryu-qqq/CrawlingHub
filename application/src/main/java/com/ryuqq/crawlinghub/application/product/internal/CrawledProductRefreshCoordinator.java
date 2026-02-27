package com.ryuqq.crawlinghub.application.product.internal;

import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawledProduct 재갱신 Coordinator
 *
 * <p>sellerId별 상품 그룹에 대해 scheduler/seller 조회 후 DETAIL + OPTION 크롤 태스크 커맨드를 생성합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductRefreshCoordinator {

    private static final Logger log =
            LoggerFactory.getLogger(CrawledProductRefreshCoordinator.class);

    private final CrawlSchedulerReadManager crawlSchedulerReadManager;
    private final SellerReadManager sellerReadManager;

    public CrawledProductRefreshCoordinator(
            CrawlSchedulerReadManager crawlSchedulerReadManager,
            SellerReadManager sellerReadManager) {
        this.crawlSchedulerReadManager = crawlSchedulerReadManager;
        this.sellerReadManager = sellerReadManager;
    }

    /**
     * sellerId별 상품 그룹에 대해 DETAIL + OPTION 태스크 커맨드 생성
     *
     * @param sellerId 셀러 ID
     * @param products 해당 셀러의 상품 목록
     * @return 생성된 커맨드 목록 (scheduler/seller가 없으면 빈 리스트)
     */
    public List<CreateCrawlTaskCommand> buildRefreshCommands(
            SellerId sellerId, List<CrawledProduct> products) {

        List<CrawlScheduler> schedulers =
                crawlSchedulerReadManager.findActiveSchedulersBySellerId(sellerId);
        if (schedulers.isEmpty()) {
            log.debug("활성 스케줄러 없음: sellerId={}", sellerId.value());
            return List.of();
        }
        CrawlScheduler scheduler = schedulers.get(0);

        Optional<Seller> sellerOpt = sellerReadManager.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            log.debug("셀러 조회 실패: sellerId={}", sellerId.value());
            return List.of();
        }
        String mustItSellerName = sellerOpt.get().getMustItSellerNameValue();

        List<CreateCrawlTaskCommand> commands = new ArrayList<>();
        for (CrawledProduct product : products) {
            commands.add(
                    CreateCrawlTaskCommand.forDetail(
                            scheduler.getCrawlSchedulerIdValue(),
                            sellerId.value(),
                            mustItSellerName,
                            product.getItemNo()));
            commands.add(
                    CreateCrawlTaskCommand.forOption(
                            scheduler.getCrawlSchedulerIdValue(),
                            sellerId.value(),
                            mustItSellerName,
                            product.getItemNo()));
        }
        return commands;
    }
}
