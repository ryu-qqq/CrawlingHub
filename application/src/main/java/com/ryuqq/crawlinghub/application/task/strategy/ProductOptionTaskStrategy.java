package com.ryuqq.crawlinghub.application.task.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ryuqq.crawlinghub.application.crawl.result.manager.CrawlResultManager;
import com.ryuqq.crawlinghub.application.product.manager.ProductManager;
import com.ryuqq.crawlinghub.application.task.dto.output.ProductOptionOutput;
import com.ryuqq.crawlinghub.application.task.facade.CrawlerFacade;
import com.ryuqq.crawlinghub.application.task.manager.TaskManager;
import com.ryuqq.crawlinghub.application.task.manager.TaskMessageOutboxManager;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.task.Task;
import org.springframework.stereotype.Component;

/**
 * PRODUCT_OPTION Task 처리 전략
 *
 * <p>역할: 개별 상품 옵션 정보 조회 및 저장 (Leaf Task)
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>CrawlerFacade를 통한 API 호출: GET /items/{itemNo}/options</li>
 *   <li>ProductOptionOutput에서 옵션 목록 추출</li>
 *   <li>ProductManager.processOptionData() 호출 (해시값 기반 변경 감지)</li>
 *   <li>후속 Task 없음 (leaf task)</li>
 * </ol>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ProductOptionTaskStrategy extends AbstractTaskStrategy<ProductOptionOutput> {

    private final ProductManager productManager;

    public ProductOptionTaskStrategy(
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
    protected Class<ProductOptionOutput> getOutputType() {
        return ProductOptionOutput.class;
    }

    @Override
    protected void executeTask(Task task, ProductOptionOutput output) {
        log.info("PRODUCT_OPTION Task 실행. taskId={}", task.getIdValue());

        // 1. 옵션 통계 확인
        int totalOptions = output.getTotalOptionCount();
        int availableOptions = output.getAvailableOptionCount();

        log.info("옵션 통계: 전체={}, 재고있음={}", totalOptions, availableOptions);

        // 2. itemNo 추출 (첫 번째 옵션에서)
        if (output.options().isEmpty()) {
            log.warn("옵션 목록이 비어있습니다. taskId={}", task.getIdValue());
            return;
        }

        String itemNo = String.valueOf(output.options().getFirst().itemNo());

        // 3. 셀러 정보 추출
        MustitSellerId sellerId = MustitSellerId.of(task.getSellerIdValue());

        try {
            // 4. Product Option 데이터 저장
            String rawData = convertToJson(output);
            productManager.processOptionData(itemNo, sellerId, rawData);

            log.info("옵션 데이터 저장 완료. itemNo={}, optionCount={}", itemNo, totalOptions);

        } catch (Exception e) {
            log.error("옵션 데이터 처리 실패. itemNo={}, error={}", itemNo, e.getMessage(), e);
            throw new RuntimeException("옵션 데이터 처리 실패: " + itemNo, e);
        }

        // 5. 후속 Task 없음 (leaf task)
        log.info("PRODUCT_OPTION Task 완료. itemNo={}, 저장된 옵션 수: {}", itemNo, totalOptions);
    }

    /**
     * ProductOptionOutput 객체를 JSON 문자열로 변환
     */
    private String convertToJson(ProductOptionOutput output) {
        try {
            return objectMapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}
