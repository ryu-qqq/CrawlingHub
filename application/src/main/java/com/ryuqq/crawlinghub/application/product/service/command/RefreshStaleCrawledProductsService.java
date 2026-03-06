package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.execution.internal.FollowUpTaskCreator;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductRefreshCoordinator;
import com.ryuqq.crawlinghub.application.product.port.in.command.RefreshStaleCrawledProductsUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 갱신이 오래된 CrawledProduct 재크롤링 태스크 생성 서비스
 *
 * <p>stale 상품 조회 → sellerId별 그룹핑 → Coordinator 위임 → 일괄 태스크 생성
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RefreshStaleCrawledProductsService implements RefreshStaleCrawledProductsUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RefreshStaleCrawledProductsService.class);

    private final CrawledProductQueryPort crawledProductQueryPort;
    private final CrawledProductRefreshCoordinator refreshCoordinator;
    private final FollowUpTaskCreator followUpTaskCreator;

    public RefreshStaleCrawledProductsService(
            CrawledProductQueryPort crawledProductQueryPort,
            CrawledProductRefreshCoordinator refreshCoordinator,
            FollowUpTaskCreator followUpTaskCreator) {
        this.crawledProductQueryPort = crawledProductQueryPort;
        this.refreshCoordinator = refreshCoordinator;
        this.followUpTaskCreator = followUpTaskCreator;
    }

    @Override
    public int execute(int batchSize) {
        List<CrawledProduct> staleProducts = crawledProductQueryPort.findStaleProducts(batchSize);
        if (staleProducts.isEmpty()) {
            log.info("갱신 대상 상품 없음");
            return 0;
        }

        Map<Long, List<CrawledProduct>> grouped =
                staleProducts.stream()
                        .collect(Collectors.groupingBy(CrawledProduct::getSellerIdValue));

        List<CreateCrawlTaskCommand> allCommands = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            SellerId sellerId = SellerId.of(entry.getKey());
            List<CreateCrawlTaskCommand> commands =
                    refreshCoordinator.buildRefreshCommands(sellerId, entry.getValue());
            allCommands.addAll(commands);
        }

        if (!allCommands.isEmpty()) {
            followUpTaskCreator.executeBatch(allCommands);
        }

        log.info(
                "상품 갱신 태스크 생성 완료: staleProducts={}, sellerGroups={}, commands={}",
                staleProducts.size(),
                grouped.size(),
                allCommands.size());

        return allCommands.size();
    }
}
