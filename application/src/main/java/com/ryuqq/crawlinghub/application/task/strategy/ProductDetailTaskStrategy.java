package com.ryuqq.crawlinghub.application.task.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ryuqq.crawlinghub.application.crawl.result.manager.CrawlResultManager;
import com.ryuqq.crawlinghub.application.product.manager.ProductManager;
import com.ryuqq.crawlinghub.application.task.dto.output.ProductDetailOutput;
import com.ryuqq.crawlinghub.application.task.facade.CrawlerFacade;
import com.ryuqq.crawlinghub.application.task.manager.TaskManager;
import com.ryuqq.crawlinghub.application.task.manager.TaskMessageOutboxManager;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.task.Task;
import org.springframework.stereotype.Component;

/**
 * PRODUCT_DETAIL Task 처리 전략
 *
 * <p>역할: 개별 상품 상세 정보 조회 및 저장 (Leaf Task)
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>CrawlerFacade를 통한 API 호출: GET /items/{itemNo}</li>
 *   <li>ProductDetailOutput에서 상품 정보 추출 (ProductInfoModule, ProductBannersModule)</li>
 *   <li>ProductManager.processDetailData() 호출 (해시값 기반 변경 감지)</li>
 *   <li>후속 Task 없음 (leaf task)</li>
 * </ol>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ProductDetailTaskStrategy extends AbstractTaskStrategy<ProductDetailOutput> {

    private final ProductManager productManager;

    public ProductDetailTaskStrategy(
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
    protected Class<ProductDetailOutput> getOutputType() {
        return ProductDetailOutput.class;
    }

    @Override
    protected void executeTask(Task task, ProductDetailOutput output) {
        log.info("PRODUCT_DETAIL Task 실행. taskId={}", task.getIdValue());

        // 1. 상품 기본 정보 추출
        ProductDetailOutput.ModuleData productInfo = output.getProductInfo();
        String itemNo = String.valueOf(productInfo.itemNo());

        log.info("상품 정보: itemNo={}, itemName={}, salePrice={}",
            itemNo, productInfo.itemName(), productInfo.salePrice());

        // 2. 이미지 정보 추출
        var images = output.getImages();
        log.info("이미지 수: {}", images.size());

        // 3. 셀러 정보 추출
        MustitSellerId sellerId = MustitSellerId.of(task.getSellerIdValue());

        try {
            // 4. Product Detail 데이터 저장
            String rawData = convertToJson(output);
            productManager.processDetailData(itemNo, sellerId, rawData);

            log.info("상세 데이터 저장 완료. itemNo={}", itemNo);

        } catch (Exception e) {
            log.error("상세 데이터 처리 실패. itemNo={}, error={}", itemNo, e.getMessage(), e);
            throw new RuntimeException("상세 데이터 처리 실패: " + itemNo, e);
        }

        // 5. 후속 Task 없음 (leaf task)
        log.info("PRODUCT_DETAIL Task 완료. itemNo={}", itemNo);
    }

    /**
     * ProductDetailOutput 객체를 JSON 문자열로 변환
     */
    private String convertToJson(ProductDetailOutput output) {
        try {
            return objectMapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}
