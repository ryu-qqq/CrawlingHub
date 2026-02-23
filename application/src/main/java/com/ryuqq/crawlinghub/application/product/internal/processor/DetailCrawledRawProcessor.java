package com.ryuqq.crawlinghub.application.product.internal.processor;

import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductCoordinator;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import org.springframework.stereotype.Component;

/**
 * DETAIL 타입 CrawledRaw 가공 프로세서
 *
 * <p>ProductDetailInfo를 역직렬화하여 기존 상품을 업데이트합니다. 상품이 없으면 스킵합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
class DetailCrawledRawProcessor implements CrawledRawProcessor {

    private final CrawledRawMapper crawledRawMapper;
    private final CrawledProductFactory crawledProductFactory;
    private final CrawledProductCoordinator coordinator;

    DetailCrawledRawProcessor(
            CrawledRawMapper crawledRawMapper,
            CrawledProductFactory crawledProductFactory,
            CrawledProductCoordinator coordinator) {
        this.crawledRawMapper = crawledRawMapper;
        this.crawledProductFactory = crawledProductFactory;
        this.coordinator = coordinator;
    }

    @Override
    public CrawlType supportedType() {
        return CrawlType.DETAIL;
    }

    @Override
    public void process(CrawledRaw raw) {
        SellerId sellerId = SellerId.of(raw.getSellerId());
        long itemNo = raw.getItemNo();
        ProductDetailInfo detailInfo = crawledRawMapper.toProductDetailInfo(raw.getRawData());
        DetailCrawlData crawlData = crawledProductFactory.createDetailCrawlData(detailInfo);

        coordinator.updateExistingAndSync(
                sellerId, itemNo, product -> product.updateFromDetailCrawlData(crawlData));
    }
}
