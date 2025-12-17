package com.ryuqq.crawlinghub.application.product.facade;

import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.application.sync.port.in.command.RequestSyncUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * OPTION 처리 Facade
 *
 * <p>CrawledProductTransactionManager와 RequestSyncUseCase를 조합하여 OPTION 크롤링 결과 처리를 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class OptionProcessFacade {

    private final CrawledProductTransactionManager crawledProductManager;
    private final RequestSyncUseCase requestSyncUseCase;

    public OptionProcessFacade(
            CrawledProductTransactionManager crawledProductManager,
            RequestSyncUseCase requestSyncUseCase) {
        this.crawledProductManager = crawledProductManager;
        this.requestSyncUseCase = requestSyncUseCase;
    }

    /**
     * OPTION 크롤링 데이터로 CrawledProduct 업데이트 및 동기화 요청
     *
     * @param existing 기존 CrawledProduct
     * @param crawlData OPTION 크롤링 데이터 VO
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct updateAndRequestSync(CrawledProduct existing, OptionCrawlData crawlData) {
        CrawledProduct updated =
                crawledProductManager.updateFromOptionCrawlData(existing, crawlData);

        // 외부 동기화 가능 여부 확인 및 요청
        requestSyncUseCase.requestIfReady(updated);

        return updated;
    }
}
