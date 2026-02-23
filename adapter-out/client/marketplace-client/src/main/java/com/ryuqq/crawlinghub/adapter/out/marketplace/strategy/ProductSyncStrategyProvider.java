package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * SyncType별 ProductSyncStrategy를 제공하는 Provider
 *
 * <p>Spring이 주입한 모든 ProductSyncStrategy를 EnumMap으로 관리하여 O(1) 조회를 보장합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductSyncStrategyProvider {

    private final Map<SyncType, ProductSyncStrategy> strategyMap;

    public ProductSyncStrategyProvider(List<ProductSyncStrategy> strategies) {
        this.strategyMap = new EnumMap<>(SyncType.class);
        for (ProductSyncStrategy strategy : strategies) {
            strategyMap.put(strategy.supportedType(), strategy);
        }
    }

    public ProductSyncStrategy getStrategy(SyncType type) {
        ProductSyncStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 SyncType: " + type);
        }
        return strategy;
    }
}
