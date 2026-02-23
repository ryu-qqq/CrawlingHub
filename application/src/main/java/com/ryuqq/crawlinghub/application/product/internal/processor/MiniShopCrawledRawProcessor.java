package com.ryuqq.crawlinghub.application.product.internal.processor;

import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductCoordinator;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import org.springframework.stereotype.Component;

/**
 * MINI_SHOP 타입 CrawledRaw 가공 프로세서
 *
 * <p>MiniShopItem을 역직렬화하여 신규 생성 또는 기존 업데이트를 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
class MiniShopCrawledRawProcessor implements CrawledRawProcessor {

    private final CrawledRawMapper crawledRawMapper;
    private final CrawledProductFactory crawledProductFactory;
    private final CrawledProductCoordinator coordinator;

    MiniShopCrawledRawProcessor(
            CrawledRawMapper crawledRawMapper,
            CrawledProductFactory crawledProductFactory,
            CrawledProductCoordinator coordinator) {
        this.crawledRawMapper = crawledRawMapper;
        this.crawledProductFactory = crawledProductFactory;
        this.coordinator = coordinator;
    }

    @Override
    public CrawlType supportedType() {
        return CrawlType.MINI_SHOP;
    }

    @Override
    public void process(CrawledRaw raw) {
        SellerId sellerId = SellerId.of(raw.getSellerId());
        MiniShopItem item = crawledRawMapper.toMiniShopItem(raw.getRawData());
        MiniShopCrawlData crawlData = crawledProductFactory.createMiniShopCrawlData(sellerId, item);

        coordinator.createOrUpdate(
                sellerId,
                item.itemNo(),
                product -> product.updateFromMiniShopCrawlData(crawlData),
                () -> CrawledProduct.fromMiniShopCrawlData(crawlData));
    }
}
