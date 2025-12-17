package com.ryuqq.crawlinghub.application.product.port.out.client;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;

/**
 * 외부 상품 서버 연동 Port (Port Out - External)
 *
 * <p>크롤링된 상품 정보를 외부 상품 서버에 등록/갱신합니다.
 *
 * <p>Request DTO 생성은 Adapter 구현체에서 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ExternalProductServerClient {

    /**
     * 상품 신규 등록
     *
     * @param outbox 동기화 Outbox (idempotencyKey 포함)
     * @param product 크롤링된 상품 정보
     * @return 등록 결과 (외부 상품 ID 포함)
     */
    ProductSyncResult createProduct(CrawledProductSyncOutbox outbox, CrawledProduct product);

    /**
     * 상품 정보 갱신
     *
     * @param outbox 동기화 Outbox (externalProductId, idempotencyKey 포함)
     * @param product 크롤링된 상품 정보
     * @return 갱신 결과
     */
    ProductSyncResult updateProduct(CrawledProductSyncOutbox outbox, CrawledProduct product);

    /**
     * 상품 존재 여부 확인
     *
     * @param externalProductId 외부 상품 ID
     * @return 존재하면 true
     */
    boolean existsProduct(Long externalProductId);
}
