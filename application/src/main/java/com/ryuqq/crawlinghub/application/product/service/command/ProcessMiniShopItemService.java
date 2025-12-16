package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.product.dto.bundle.MiniShopProcessBundle;
import com.ryuqq.crawlinghub.application.product.facade.MiniShopProcessFacade;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessMiniShopItemUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MINI_SHOP 크롤링 결과 처리 Service
 *
 * <p>Factory 패턴을 통해 MiniShopProcessBundle을 생성하고, Facade를 통해 저장 및 이미지 업로드를 처리합니다.
 *
 * <p><strong>Bundle 패턴 적용</strong>:
 *
 * <ul>
 *   <li>Factory에서 MiniShopCrawlData + ImageUploadData를 Bundle로 묶어 생성
 *   <li>Facade에서 Bundle 단위로 처리 (Outbox 생성, Event 발행 포함)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessMiniShopItemService implements ProcessMiniShopItemUseCase {

    private final CrawledProductReadManager crawledProductReadManager;
    private final CrawledProductFactory crawledProductFactory;
    private final MiniShopProcessFacade miniShopProcessFacade;

    public ProcessMiniShopItemService(
            CrawledProductReadManager crawledProductReadManager,
            CrawledProductFactory crawledProductFactory,
            MiniShopProcessFacade miniShopProcessFacade) {
        this.crawledProductReadManager = crawledProductReadManager;
        this.crawledProductFactory = crawledProductFactory;
        this.miniShopProcessFacade = miniShopProcessFacade;
    }

    @Override
    @Transactional
    public CrawledProduct process(SellerId sellerId, MiniShopItem item) {
        // Factory로 Bundle 생성 후 Facade로 처리
        MiniShopProcessBundle bundle =
                crawledProductFactory.createMiniShopProcessBundle(sellerId, item);

        Optional<CrawledProduct> existingOpt =
                crawledProductReadManager.findBySellerIdAndItemNo(sellerId, item.itemNo());

        if (existingOpt.isPresent()) {
            // 기존 상품 업데이트
            return miniShopProcessFacade.updateAndRequestImageUpload(existingOpt.get(), bundle);
        } else {
            // 신규 상품 생성
            return miniShopProcessFacade.createAndRequestImageUpload(bundle);
        }
    }
}
