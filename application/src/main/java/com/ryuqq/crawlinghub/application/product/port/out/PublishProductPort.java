package com.ryuqq.crawlinghub.application.product.port.out;

import com.ryuqq.crawlinghub.domain.product.CrawledProduct;

/**
 * Product 외부 발행 Port (Outbound)
 *
 * <p>완성된 상품 또는 변경된 상품을 외부 시스템(Kafka, SQS 등)으로 발행
 *
 * <p>발행 조건:
 * <ul>
 *   <li>최초 등록: 상품이 완성(COMPLETE) 상태일 때</li>
 *   <li>데이터 변경: 해시값 불일치 감지 시</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface PublishProductPort {

    /**
     * 완성된 상품 발행
     *
     * <p>미니샵 + 상세 + 옵션 모두 존재하는 상품
     *
     * @param product 발행할 상품
     */
    void publishCompletedProduct(CrawledProduct product);

    /**
     * 변경된 상품 발행
     *
     * <p>해시값 불일치로 인한 데이터 변경 감지
     *
     * @param product 발행할 상품
     */
    void publishUpdatedProduct(CrawledProduct product);
}
