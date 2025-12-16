package com.ryuqq.crawlinghub.application.product.service.query;

import com.ryuqq.crawlinghub.application.product.port.in.query.GetCrawledProductUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledProduct 조회 Service
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class GetCrawledProductService implements GetCrawledProductUseCase {

    private final CrawledProductQueryPort crawledProductQueryPort;

    public GetCrawledProductService(CrawledProductQueryPort crawledProductQueryPort) {
        this.crawledProductQueryPort = crawledProductQueryPort;
    }

    @Override
    public Optional<CrawledProduct> findById(CrawledProductId crawledProductId) {
        return crawledProductQueryPort.findById(crawledProductId);
    }

    @Override
    public Optional<CrawledProduct> findBySellerIdAndItemNo(SellerId sellerId, long itemNo) {
        return crawledProductQueryPort.findBySellerIdAndItemNo(sellerId, itemNo);
    }

    @Override
    public List<CrawledProduct> findBySellerId(SellerId sellerId) {
        return crawledProductQueryPort.findBySellerId(sellerId);
    }
}
