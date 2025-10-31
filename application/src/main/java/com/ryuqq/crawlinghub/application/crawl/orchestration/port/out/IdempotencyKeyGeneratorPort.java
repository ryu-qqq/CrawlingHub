package com.ryuqq.crawlinghub.application.crawl.orchestration.port.out;

import com.ryuqq.crawlinghub.domain.crawl.task.TaskType;

/**
 * 멱등성 키 생성 Port
 *
 * <p>CrawlTask 생성 시 중복 방지를 위한 멱등성 키를 생성합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface IdempotencyKeyGeneratorPort {

    /**
     * 멱등성 키 생성
     *
     * <p>Format: {sellerId}:{taskType}:{pageNumber}:{itemNo}:{timestamp}
     * <ul>
     *   <li>MINI_SHOP: sellerId:MINI_SHOP:0::timestamp</li>
     *   <li>PRODUCT_DETAIL: sellerId:PRODUCT_DETAIL:null:12345:timestamp</li>
     * </ul>
     *
     * @param sellerId   셀러 ID
     * @param taskType   태스크 유형
     * @param pageNumber 페이지 번호 (MINI_SHOP 전용, nullable)
     * @param itemNo     상품 번호 (PRODUCT_DETAIL 전용, nullable)
     * @return 멱등성 키
     */
    String generate(Long sellerId, TaskType taskType, Integer pageNumber, String itemNo);
}
