package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.adapter.out.marketplace.client.MarketPlaceClient;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateDescriptionRequest;
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
 * 상세설명 갱신 전략
 *
 * <p>MarketPlace PATCH .../description 호출
 */
@Component
public class UpdateDescriptionSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpdateDescriptionSyncStrategy.class);

    private final MarketPlaceClient marketPlaceClient;
    private final InboundProductRequestMapper requestMapper;

    public UpdateDescriptionSyncStrategy(
            MarketPlaceClient marketPlaceClient, InboundProductRequestMapper requestMapper) {
        this.marketPlaceClient = marketPlaceClient;
        this.requestMapper = requestMapper;
    }

    @Override
    public SyncType supportedType() {
        return SyncType.UPDATE_DESCRIPTION;
    }

    @Override
    public ProductSyncResult execute(
            CrawledProductSyncOutbox outbox, CrawledProduct product, Seller seller) {
        try {
            long inboundSourceId = requestMapper.getInboundSourceId();
            String externalProductCode = requestMapper.getExternalProductCode(outbox);
            UpdateDescriptionRequest request = requestMapper.toUpdateDescriptionRequest(product);

            log.info(
                    "[UPDATE_DESCRIPTION] 상세설명 수정 요청 - outboxId={}, externalProductCode={}",
                    outbox.getId(),
                    externalProductCode);

            marketPlaceClient.updateDescription(inboundSourceId, externalProductCode, request);

            log.info(
                    "[UPDATE_DESCRIPTION] 상세설명 수정 성공 - outboxId={}, externalProductCode={}",
                    outbox.getId(),
                    externalProductCode);

            return ProductSyncResult.success(outbox.getExternalProductId());
        } catch (Exception e) {
            log.error("[UPDATE_DESCRIPTION] 상세설명 수정 실패 - outboxId={}", outbox.getId(), e);
            return ProductSyncResult.failure("UPDATE_DESCRIPTION_FAILED", e.getMessage());
        }
    }
}
