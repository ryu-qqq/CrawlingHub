package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.adapter.out.marketplace.client.MarketPlaceClient;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdatePriceRequest;
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
 * 가격 갱신 전략
 *
 * <p>MarketPlace PATCH .../price 호출
 */
@Component
public class UpdatePriceSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpdatePriceSyncStrategy.class);

    private final MarketPlaceClient marketPlaceClient;
    private final InboundProductRequestMapper requestMapper;

    public UpdatePriceSyncStrategy(
            MarketPlaceClient marketPlaceClient, InboundProductRequestMapper requestMapper) {
        this.marketPlaceClient = marketPlaceClient;
        this.requestMapper = requestMapper;
    }

    @Override
    public SyncType supportedType() {
        return SyncType.UPDATE_PRICE;
    }

    @Override
    public ProductSyncResult execute(
            CrawledProductSyncOutbox outbox, CrawledProduct product, Seller seller) {
        try {
            long inboundSourceId = requestMapper.getInboundSourceId();
            String externalProductCode = requestMapper.getExternalProductCode(outbox);
            UpdatePriceRequest request = requestMapper.toUpdatePriceRequest(product);

            log.info(
                    "[UPDATE_PRICE] 가격 수정 요청 - outboxId={}, externalProductCode={},"
                            + " regularPrice={}, currentPrice={}",
                    outbox.getId(),
                    externalProductCode,
                    request.regularPrice(),
                    request.currentPrice());

            marketPlaceClient.updatePrice(inboundSourceId, externalProductCode, request);

            log.info(
                    "[UPDATE_PRICE] 가격 수정 성공 - outboxId={}, externalProductCode={}",
                    outbox.getId(),
                    externalProductCode);

            return ProductSyncResult.success(outbox.getExternalProductId());
        } catch (Exception e) {
            log.error("[UPDATE_PRICE] 가격 수정 실패 - outboxId={}", outbox.getId(), e);
            return ProductSyncResult.failure("UPDATE_PRICE_FAILED", e.getMessage());
        }
    }
}
