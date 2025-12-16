package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.product.dto.bundle.DetailProcessBundle;
import com.ryuqq.crawlinghub.application.product.facade.DetailProcessFacade;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessDetailInfoUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DETAIL 크롤링 결과 처리 Service
 *
 * <p>Factory 패턴을 통해 DetailProcessBundle을 생성하고, Facade를 통해 업데이트, 이미지 업로드, 동기화를 처리합니다.
 *
 * <p><strong>Bundle 패턴 적용</strong>:
 *
 * <ul>
 *   <li>Factory에서 DetailCrawlData + ImageUploadData를 Bundle로 묶어 생성
 *   <li>Facade에서 Bundle 단위로 처리 (Outbox 생성, Event 발행 포함)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessDetailInfoService implements ProcessDetailInfoUseCase {

    private final CrawledProductReadManager crawledProductReadManager;
    private final CrawledProductFactory crawledProductFactory;
    private final DetailProcessFacade detailProcessFacade;

    public ProcessDetailInfoService(
            CrawledProductReadManager crawledProductReadManager,
            CrawledProductFactory crawledProductFactory,
            DetailProcessFacade detailProcessFacade) {
        this.crawledProductReadManager = crawledProductReadManager;
        this.crawledProductFactory = crawledProductFactory;
        this.detailProcessFacade = detailProcessFacade;
    }

    @Override
    @Transactional
    public Optional<CrawledProduct> process(
            SellerId sellerId, long itemNo, ProductDetailInfo detailInfo) {
        Optional<CrawledProduct> productOpt =
                crawledProductReadManager.findBySellerIdAndItemNo(sellerId, itemNo);

        if (productOpt.isEmpty()) {
            return Optional.empty();
        }

        CrawledProduct existing = productOpt.get();

        // Factory로 Bundle 생성 후 Facade로 처리
        DetailProcessBundle bundle = crawledProductFactory.createDetailProcessBundle(detailInfo);
        CrawledProduct updated =
                detailProcessFacade.updateAndRequestUploadAndSync(existing, bundle);

        return Optional.of(updated);
    }
}
