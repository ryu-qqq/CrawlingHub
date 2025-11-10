package com.ryuqq.crawlinghub.application.crawl.orchestration.dto.command;

import com.ryuqq.crawlinghub.application.task.dto.command.ProductDetailCommand;

/**
 * ProductDetailCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class ProductDetailCommandFixture {

    private static final Long DEFAULT_TASK_ID = 1L;
    private static final String DEFAULT_ITEM_NO = "12345";
    private static final String DEFAULT_RESPONSE_DATA = "{\"itemNo\":\"12345\",\"name\":\"테스트상품\",\"price\":10000}";

    /**
     * 기본 ProductDetailCommand 생성
     *
     * @return ProductDetailCommand
     */
    public static ProductDetailCommand create() {
        return new ProductDetailCommand(
            DEFAULT_TASK_ID,
            DEFAULT_ITEM_NO,
            DEFAULT_RESPONSE_DATA
        );
    }

    /**
     * 특정 태스크 ID로 ProductDetailCommand 생성
     *
     * @param taskId 태스크 ID
     * @return ProductDetailCommand
     */
    public static ProductDetailCommand createWithTaskId(Long taskId) {
        return new ProductDetailCommand(
            taskId,
            DEFAULT_ITEM_NO,
            DEFAULT_RESPONSE_DATA
        );
    }

    /**
     * 특정 상품 번호로 ProductDetailCommand 생성
     *
     * @param itemNo 상품 번호
     * @return ProductDetailCommand
     */
    public static ProductDetailCommand createWithItemNo(String itemNo) {
        return new ProductDetailCommand(
            DEFAULT_TASK_ID,
            itemNo,
            DEFAULT_RESPONSE_DATA
        );
    }

    /**
     * 특정 응답 데이터로 ProductDetailCommand 생성
     *
     * @param responseData 응답 데이터
     * @return ProductDetailCommand
     */
    public static ProductDetailCommand createWithResponseData(String responseData) {
        return new ProductDetailCommand(
            DEFAULT_TASK_ID,
            DEFAULT_ITEM_NO,
            responseData
        );
    }

    /**
     * 완전한 커스텀 ProductDetailCommand 생성
     *
     * @param taskId       태스크 ID
     * @param itemNo       상품 번호
     * @param responseData 응답 데이터
     * @return ProductDetailCommand
     */
    public static ProductDetailCommand createCustom(
        Long taskId,
        String itemNo,
        String responseData
    ) {
        return new ProductDetailCommand(taskId, itemNo, responseData);
    }
}
