package com.ryuqq.crawlinghub.application.task.strategy;

import com.ryuqq.crawlinghub.application.crawl.result.manager.CrawlResultManager;
import com.ryuqq.crawlinghub.application.seller.component.SellerManager;
import com.ryuqq.crawlinghub.application.task.dto.output.MiniShopOutput;
import com.ryuqq.crawlinghub.application.task.facade.CrawlerFacade;
import com.ryuqq.crawlinghub.application.task.manager.TaskManager;
import com.ryuqq.crawlinghub.application.task.manager.TaskMessageOutboxManager;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerName;
import com.ryuqq.crawlinghub.domain.task.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * META Task 처리 전략
 *
 * <p>역할: 전체 상품 개수 조회 후 MINI_SHOP Task 동적 생성 + 셀러 상품 수 업데이트
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>CrawlerFacade를 통한 API 호출: GET /items?sellerId=X&pageNo=0&pageSize=1</li>
 *   <li>MiniShopOutput에서 totalCount 추출</li>
 *   <li>셀러 상품 수 업데이트 (SellerManager.updateProductCountWithHistory)</li>
 *   <li>페이지 수 계산: totalPages = ceil(totalCount / 500)</li>
 *   <li>MINI_SHOP Task × N개 동적 생성 (pageNo: 0 ~ totalPages-1)</li>
 *   <li>TaskMessage Outbox 저장 후 이벤트 발행 (SQS)</li>
 * </ol>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class MetaTaskStrategy extends AbstractTaskStrategy<MiniShopOutput> {

    private static final int MINI_SHOP_PAGE_SIZE = 500;

    private final SellerManager sellerManager;

    public MetaTaskStrategy(
        TaskManager taskManager,
        TaskMessageOutboxManager taskMessageOutboxManager,
        CrawlerFacade crawlerFacade,
        CrawlResultManager crawlResultManager,
        ObjectMapper objectMapper,
        SellerManager sellerManager
    ) {
        super(taskManager, taskMessageOutboxManager, crawlerFacade, crawlResultManager, objectMapper);
        this.sellerManager = sellerManager;
    }

    @Override
    protected Class<MiniShopOutput> getOutputType() {
        return MiniShopOutput.class;
    }

    @Override
    protected void executeTask(Task task, MiniShopOutput output) {
        log.info("META Task 실행. taskId={}, totalCount={}",
            task.getIdValue(), output.getTotalCount());

        MustitSellerId sellerId = MustitSellerId.of(task.getSellerIdValue());
        Integer totalCount = output.getTotalCount();

        // 1. 셀러 상품 수 업데이트 (SellerManager)
        try {
            MustitSeller seller = sellerManager.loadSeller(sellerId.value());
            sellerManager.updateProductCountWithHistory(seller, totalCount);
            log.info("셀러 상품 수 업데이트 완료. sellerId={}, totalCount={}", sellerId.value(), totalCount);
        } catch (Exception e) {
            log.error("셀러 상품 수 업데이트 실패. sellerId={}, error={}", sellerId.value(), e.getMessage(), e);
            // 상품 수 업데이트 실패해도 Task 생성은 계속 진행
        }

        // 2. 페이지 수 계산
        int totalPages = output.calculateTotalPages(MINI_SHOP_PAGE_SIZE);

        log.info("META Task: 총 {}개 페이지 감지. MINI_SHOP Task {}개 생성 예정",
            totalPages, totalPages);

        // 3. MINI_SHOP Task 동적 생성 (페이지별)
        SellerName sellerName = task.getSellerName();
        LocalDateTime now = LocalDateTime.now();
        List<Task> miniShopTasks = new ArrayList<>();

        for (int pageNo = 0; pageNo < totalPages; pageNo++) {
            Task miniShopTask = Task.forMiniShop(
                sellerId,
                sellerName,
                pageNo,
                MINI_SHOP_PAGE_SIZE,
                task.getCrawlScheduleId(), // 부모 Task의 crawlScheduleId 전달
                com.ryuqq.crawlinghub.domain.task.TriggerType.AUTO, // Event 기반 → AUTO
                now
            );
            miniShopTasks.add(miniShopTask);

            log.debug("MINI_SHOP Task 생성: pageNo={}, url={}",
                pageNo, miniShopTask.getRequestUrlValue());
        }

        // 4. Task 저장 및 메시지 발행
        taskManager.saveTasks(miniShopTasks);
        taskMessageOutboxManager.publishTaskMessages(miniShopTasks);

        log.info("META Task 완료. 생성된 MINI_SHOP Task 수: {}", miniShopTasks.size());
    }
}
