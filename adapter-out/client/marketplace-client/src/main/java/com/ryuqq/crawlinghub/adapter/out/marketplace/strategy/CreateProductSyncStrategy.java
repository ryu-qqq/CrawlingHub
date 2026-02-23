package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 상품 신규 등록 전략 (STUB)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CreateProductSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(CreateProductSyncStrategy.class);

    private final AtomicLong externalProductIdGenerator = new AtomicLong(1_000_000L);

    @Override
    public SyncType supportedType() {
        return SyncType.CREATE;
    }

    @Override
    public ProductSyncResult execute(CrawledProductSyncOutbox outbox, CrawledProduct product) {
        Long externalProductId = externalProductIdGenerator.incrementAndGet();

        log.info(
                "[STUB] 상품 신규 등록 요청 - outboxId={}, crawledProductId={}, sellerId={}, "
                        + "itemNo={}, idempotencyKey={}, itemName={}, externalProductId={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getSellerIdValue(),
                outbox.getItemNo(),
                outbox.getIdempotencyKey(),
                product.getItemName(),
                externalProductId);

        return ProductSyncResult.success(externalProductId);
    }
}
