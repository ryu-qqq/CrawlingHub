package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;

/**
 * SyncType별 외부 API 호출 전략 인터페이스
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProductSyncStrategy {

    SyncType supportedType();

    ProductSyncResult execute(CrawledProductSyncOutbox outbox, CrawledProduct product);
}
