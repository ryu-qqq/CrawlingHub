package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.adapter.out.marketplace.client.MarketPlaceClient;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateProductsRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.mapper.InboundProductRequestMapper;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 상품 기본정보 갱신 전략
 *
 * <p>MarketPlace PATCH .../products 호출 (옵션 + 상품 기본정보 일괄 갱신)
 */
@Component
public class UpdateProductInfoSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpdateProductInfoSyncStrategy.class);

    private final MarketPlaceClient marketPlaceClient;
    private final InboundProductRequestMapper requestMapper;

    public UpdateProductInfoSyncStrategy(
            MarketPlaceClient marketPlaceClient, InboundProductRequestMapper requestMapper) {
        this.marketPlaceClient = marketPlaceClient;
        this.requestMapper = requestMapper;
    }

    @Override
    public SyncType supportedType() {
        return SyncType.UPDATE_PRODUCT_INFO;
    }

    @Override
    public ProductSyncResult execute(
            CrawledProductSyncOutbox outbox, CrawledProduct product, Seller seller) {
        try {
            long inboundSourceId = requestMapper.getInboundSourceId();
            String externalProductCode = requestMapper.getExternalProductCode(outbox);
            UpdateProductsRequest request = requestMapper.toUpdateProductsRequest(product);

            log.info(
                    "[UPDATE_PRODUCT_INFO] 상품 기본정보 수정 요청 - outboxId={},"
                            + " externalProductCode={}, optionGroupCount={}, productCount={}",
                    outbox.getId(),
                    externalProductCode,
                    request.optionGroups().size(),
                    request.products().size());

            marketPlaceClient.updateProducts(inboundSourceId, externalProductCode, request);

            log.info(
                    "[UPDATE_PRODUCT_INFO] 상품 기본정보 수정 성공 - outboxId={},"
                            + " externalProductCode={}",
                    outbox.getId(),
                    externalProductCode);

            return ProductSyncResult.success(outbox.getExternalProductId());
        } catch (Exception e) {
            log.error("[UPDATE_PRODUCT_INFO] 상품 기본정보 수정 실패 - outboxId={}", outbox.getId(), e);
            return ProductSyncResult.failure("UPDATE_PRODUCT_INFO_FAILED", e.getMessage());
        }
    }
}
