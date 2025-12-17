package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.product.facade.OptionProcessFacade;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessOptionsUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * OPTION 크롤링 결과 처리 Service
 *
 * <p>Factory 패턴을 통해 OptionCrawlData VO를 생성하고, Facade를 통해 업데이트 및 동기화를 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessOptionsService implements ProcessOptionsUseCase {

    private final CrawledProductReadManager crawledProductReadManager;
    private final CrawledProductFactory crawledProductFactory;
    private final OptionProcessFacade optionProcessFacade;

    public ProcessOptionsService(
            CrawledProductReadManager crawledProductReadManager,
            CrawledProductFactory crawledProductFactory,
            OptionProcessFacade optionProcessFacade) {
        this.crawledProductReadManager = crawledProductReadManager;
        this.crawledProductFactory = crawledProductFactory;
        this.optionProcessFacade = optionProcessFacade;
    }

    @Override
    public Optional<CrawledProduct> process(
            SellerId sellerId, long itemNo, List<ProductOption> options) {
        Optional<CrawledProduct> productOpt =
                crawledProductReadManager.findBySellerIdAndItemNo(sellerId, itemNo);

        if (productOpt.isEmpty()) {
            return Optional.empty();
        }

        CrawledProduct existing = productOpt.get();

        // Factory로 VO 생성 후 Facade로 처리
        OptionCrawlData crawlData = crawledProductFactory.createOptionCrawlData(options);
        CrawledProduct updated = optionProcessFacade.updateAndRequestSync(existing, crawlData);

        return Optional.of(updated);
    }
}
