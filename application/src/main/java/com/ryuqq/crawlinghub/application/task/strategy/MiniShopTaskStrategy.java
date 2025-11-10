package com.ryuqq.crawlinghub.application.task.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ryuqq.crawlinghub.application.crawl.result.manager.CrawlResultManager;
import com.ryuqq.crawlinghub.application.product.manager.ProductManager;
import com.ryuqq.crawlinghub.application.task.dto.output.MiniShopOutput;
import com.ryuqq.crawlinghub.application.task.facade.CrawlerFacade;
import com.ryuqq.crawlinghub.application.task.manager.TaskManager;
import com.ryuqq.crawlinghub.application.task.manager.TaskMessageOutboxManager;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerName;
import com.ryuqq.crawlinghub.domain.task.Task;
import com.ryuqq.crawlinghub.domain.task.TriggerType;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MINI_SHOP Task 처리 전략
 *
 * <p>역할: 상품 목록 조회 후 Product 저장 및 PRODUCT_DETAIL/OPTION Task 동적 생성
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>CrawlerFacade를 통한 API 호출: GET /items?sellerId=X&pageNo=Y&pageSize=500</li>
 *   <li>MiniShopOutput에서 상품 목록 추출 (itemNo 리스트)</li>
 *   <li>각 상품마다:
 *     <ul>
 *       <li>ProductManager.processMiniShopData() 호출 (최초 크롤링 판단 + 저장)</li>
 *       <li>PRODUCT_DETAIL + PRODUCT_OPTION Task 동적 생성</li>
 *     </ul>
 *   </li>
 *   <li>TaskMessage Outbox 저장 후 이벤트 발행 (SQS)</li>
 * </ol>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class MiniShopTaskStrategy extends AbstractTaskStrategy<MiniShopOutput> {

    private final ProductManager productManager;

    public MiniShopTaskStrategy(
        TaskManager taskManager,
        TaskMessageOutboxManager taskMessageOutboxManager,
        CrawlerFacade crawlerFacade,
        CrawlResultManager crawlResultManager,
        ObjectMapper objectMapper,
        ProductManager productManager
    ) {
        super(taskManager, taskMessageOutboxManager, crawlerFacade, crawlResultManager, objectMapper);
        this.productManager = productManager;

    }

    @Override
    protected Class<MiniShopOutput> getOutputType() {
        return MiniShopOutput.class;
    }

    @Override
    protected void executeTask(Task task, MiniShopOutput output) {
        log.info("MINI_SHOP Task 실행. taskId={}, totalCount={}",
            task.getIdValue(), output.getTotalCount());

        // 1. 상품 목록 추출
        List<MiniShopOutput.Item> items = output.items();
        if (items == null || items.isEmpty()) {
            log.warn("상품 목록이 비어있습니다. taskId={}", task.getIdValue());
            return;
        }

        log.info("상품 {}개 처리 시작", items.size());

        // 2. 셀러 정보 추출
        MustitSellerId sellerId = MustitSellerId.of(task.getSellerIdValue());
        SellerName sellerName = task.getSellerName();
        LocalDateTime now = LocalDateTime.now();

        // 3. 각 상품마다 처리 및 Task 생성
        List<Task> productTasks = new ArrayList<>();

        for (MiniShopOutput.Item item : items) {
            try {
                // 3-1. 미니샵 데이터 저장 (ProductManager)
                Long itemNo = item.itemNo();
                String rawData = convertToJson(item);

                productManager.processMiniShopData(itemNo, sellerId, rawData);

                log.debug("미니샵 데이터 저장 완료. itemNo={}", itemNo);

                // 3-2. PRODUCT_DETAIL + PRODUCT_OPTION Task 생성 (AUTO 트리거)
                Task detailTask = Task.forProductDetail(
                    sellerId,
                    sellerName,
                    item.itemNo(),
                    task.getCrawlScheduleId(), // 부모 Task의 crawlScheduleId 전달
                    TriggerType.AUTO, // Event 기반 → AUTO
                    now
                );
                Task optionTask = Task.forProductOption(
                    sellerId,
                    sellerName,
                    item.itemNo(),
                    task.getCrawlScheduleId(), // 부모 Task의 crawlScheduleId 전달
                    TriggerType.AUTO, // Event 기반 → AUTO
                    now
                );

                productTasks.add(detailTask);
                productTasks.add(optionTask);

            } catch (Exception e) {
                log.error("상품 처리 실패. itemNo={}, error={}", item.itemNo(), e.getMessage(), e);
                // 개별 상품 실패는 스킵하고 계속 처리
            }
        }

        // 4. Task 저장 및 메시지 발행
        if (!productTasks.isEmpty()) {
            taskManager.saveTasks(productTasks);
            taskMessageOutboxManager.publishTaskMessages(productTasks);
        }

        log.info("MINI_SHOP Task 완료. 생성된 Product Task 수: {} (Detail + Option)", productTasks.size());
    }

    /**
     * Item 객체를 JSON 문자열로 변환
     */
    private String convertToJson(MiniShopOutput.Item item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패: " + item.itemNo(), e);
        }
    }
}
