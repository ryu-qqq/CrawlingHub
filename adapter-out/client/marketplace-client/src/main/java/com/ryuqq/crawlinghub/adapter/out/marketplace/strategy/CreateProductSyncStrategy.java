package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.ryuqq.crawlinghub.adapter.out.marketplace.client.MarketPlaceClient;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response.InboundProductConversionResponse;
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
 * 상품 신규 등록 전략
 *
 * <p>MarketPlace POST /api/v1/market/inbound/products 호출
 */
@Component
public class CreateProductSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(CreateProductSyncStrategy.class);

    private final MarketPlaceClient marketPlaceClient;
    private final InboundProductRequestMapper requestMapper;

    public CreateProductSyncStrategy(
            MarketPlaceClient marketPlaceClient, InboundProductRequestMapper requestMapper) {
        this.marketPlaceClient = marketPlaceClient;
        this.requestMapper = requestMapper;
    }

    @Override
    public SyncType supportedType() {
        return SyncType.CREATE;
    }

    @Override
    public ProductSyncResult execute(
            CrawledProductSyncOutbox outbox, CrawledProduct product, Seller seller) {
        try {
            ReceiveInboundProductRequest request =
                    requestMapper.toReceiveRequest(outbox, product, seller);

            log.info(
                    "[CREATE] 인바운드 상품 수신 요청 - outboxId={}, externalProductCode={}, sellerId={}",
                    outbox.getId(),
                    request.externalProductCode(),
                    request.sellerId());

            InboundProductConversionResponse response =
                    marketPlaceClient.receiveInboundProduct(request);

            if (response.inboundProductId() == null) {
                log.warn(
                        "[CREATE] 인바운드 상품 수신 응답에 inboundProductId 누락 - outboxId={}, status={},"
                                + " action={}",
                        outbox.getId(),
                        response.status(),
                        response.action());
                return ProductSyncResult.failure(
                        "CREATE_NO_ID", "MarketPlace 응답에 inboundProductId가 누락되었습니다 (매핑 실패 가능성)");
            }

            log.info(
                    "[CREATE] 인바운드 상품 수신 성공 - inboundProductId={}, status={}, action={}",
                    response.inboundProductId(),
                    response.status(),
                    response.action());

            return ProductSyncResult.success(response.inboundProductId());
        } catch (Exception e) {
            log.error("[CREATE] 인바운드 상품 수신 실패 - outboxId={}", outbox.getId(), e);
            return ProductSyncResult.failure("CREATE_FAILED", e.getMessage());
        }
    }
}
