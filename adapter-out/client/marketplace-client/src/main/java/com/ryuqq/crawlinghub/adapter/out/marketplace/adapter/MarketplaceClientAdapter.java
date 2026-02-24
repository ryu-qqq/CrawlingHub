package com.ryuqq.crawlinghub.adapter.out.marketplace.adapter;

import com.ryuqq.crawlinghub.adapter.out.marketplace.strategy.ProductSyncStrategyProvider;
import com.ryuqq.crawlinghub.application.common.metric.annotation.OutboundClientMetric;
import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Marketplace Client Adapter
 *
 * <p>ExternalProductServerClient Port의 구현체입니다. SyncType별 라우팅은 ProductSyncStrategyProvider에 위임합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class MarketplaceClientAdapter implements ExternalProductServerClient {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceClientAdapter.class);

    private final ProductSyncStrategyProvider strategyProvider;

    public MarketplaceClientAdapter(ProductSyncStrategyProvider strategyProvider) {
        this.strategyProvider = strategyProvider;
    }

    @OutboundClientMetric(system = "marketplace", operation = "sync_product")
    @Override
    public ProductSyncResult sync(CrawledProductSyncOutbox outbox, CrawledProduct product) {
        return strategyProvider.getStrategy(outbox.getSyncType()).execute(outbox, product);
    }
}
