package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 이미지 갱신 전략 (STUB)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UpdateImageSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpdateImageSyncStrategy.class);

    @Override
    public SyncType supportedType() {
        return SyncType.UPDATE_IMAGE;
    }

    @Override
    public ProductSyncResult execute(CrawledProductSyncOutbox outbox, CrawledProduct product) {
        log.info(
                "[STUB] 이미지 갱신 요청 - outboxId={}, crawledProductId={}, externalProductId={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getExternalProductId());

        return ProductSyncResult.success(outbox.getExternalProductId());
    }
}
