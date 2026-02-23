package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 옵션/재고 갱신 전략 (STUB)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UpdateOptionStockSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpdateOptionStockSyncStrategy.class);

    @Override
    public SyncType supportedType() {
        return SyncType.UPDATE_OPTION_STOCK;
    }

    @Override
    public ProductSyncResult execute(CrawledProductSyncOutbox outbox, CrawledProduct product) {
        log.info(
                "[STUB] 옵션/재고 갱신 요청 - outboxId={}, crawledProductId={}, externalProductId={}, "
                        + "optionCount={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getExternalProductId(),
                product.getOptions() != null ? product.getOptions().size() : 0);

        return ProductSyncResult.success(outbox.getExternalProductId());
    }
}
