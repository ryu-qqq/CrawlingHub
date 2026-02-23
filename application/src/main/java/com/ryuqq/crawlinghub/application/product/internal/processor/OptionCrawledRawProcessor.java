package com.ryuqq.crawlinghub.application.product.internal.processor;

import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductCoordinator;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * OPTION 타입 CrawledRaw 가공 프로세서
 *
 * <p>ProductOption 목록을 역직렬화하여 기존 상품을 업데이트합니다. 상품이 없으면 스킵합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
class OptionCrawledRawProcessor implements CrawledRawProcessor {

    private final CrawledRawMapper crawledRawMapper;
    private final CrawledProductFactory crawledProductFactory;
    private final CrawledProductCoordinator coordinator;

    OptionCrawledRawProcessor(
            CrawledRawMapper crawledRawMapper,
            CrawledProductFactory crawledProductFactory,
            CrawledProductCoordinator coordinator) {
        this.crawledRawMapper = crawledRawMapper;
        this.crawledProductFactory = crawledProductFactory;
        this.coordinator = coordinator;
    }

    @Override
    public CrawlType supportedType() {
        return CrawlType.OPTION;
    }

    @Override
    public void process(CrawledRaw raw) {
        SellerId sellerId = SellerId.of(raw.getSellerId());
        long itemNo = raw.getItemNo();
        List<ProductOption> options = crawledRawMapper.toProductOptions(raw.getRawData());
        OptionCrawlData crawlData = crawledProductFactory.createOptionCrawlData(options);

        coordinator.updateExistingAndSync(
                sellerId, itemNo, product -> product.updateFromOptionCrawlData(crawlData));
    }
}
