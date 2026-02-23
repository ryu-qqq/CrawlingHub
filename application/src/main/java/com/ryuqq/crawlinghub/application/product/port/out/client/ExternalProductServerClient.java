package com.ryuqq.crawlinghub.application.product.port.out.client;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;

/**
 * 외부 상품 서버 연동 Port (Port Out - External)
 *
 * <p>크롤링된 상품 정보를 외부 상품 서버에 등록/갱신합니다. SyncType별 라우팅은 Adapter 구현체 내부에서 전략 패턴으로 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ExternalProductServerClient {

    /**
     * 상품 동기화 (SyncType별 라우팅은 Adapter 내부에서 처리)
     *
     * @param outbox 동기화 Outbox (syncType, idempotencyKey 포함)
     * @param product 크롤링된 상품 정보
     * @return 동기화 결과
     */
    ProductSyncResult sync(CrawledProductSyncOutbox outbox, CrawledProduct product);
}
