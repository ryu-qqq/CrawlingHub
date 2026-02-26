package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 가격 갱신 전략 (STUB)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UpdatePriceSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpdatePriceSyncStrategy.class);

    @Override
    public SyncType supportedType() {
        return SyncType.UPDATE_PRICE;
    }

    @Override
    public ProductSyncResult execute(
            CrawledProductSyncOutbox outbox, CrawledProduct product, Seller seller) {
        log.info(
                "[STUB] 가격 갱신 요청 - outboxId={}, crawledProductId={}, externalProductId={},"
                        + " price={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getExternalProductId(),
                product.getPrice());

        return ProductSyncResult.success(outbox.getExternalProductId());
    }
}
