package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 상품 기본정보 갱신 전략 (STUB)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UpdateProductInfoSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpdateProductInfoSyncStrategy.class);

    @Override
    public SyncType supportedType() {
        return SyncType.UPDATE_PRODUCT_INFO;
    }

    @Override
    public ProductSyncResult execute(CrawledProductSyncOutbox outbox, CrawledProduct product) {
        log.info(
                "[STUB] 상품 기본정보 갱신 요청 - outboxId={}, crawledProductId={}, "
                        + "externalProductId={}, itemName={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getExternalProductId(),
                product.getItemName());

        return ProductSyncResult.success(outbox.getExternalProductId());
    }
}
