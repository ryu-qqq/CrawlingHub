package com.ryuqq.crawlinghub.adapter.out.marketplace.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryuqq.crawlinghub.adapter.out.marketplace.client.MarketPlaceClient;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.CreateProductRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response.CreateProductResponse;
import com.ryuqq.crawlinghub.adapter.out.marketplace.mapper.CreateProductRequestMapper;
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
 * <p>CreateProductRequestMapper로 요청을 조립하고, MarketPlaceClient로 외부몰 API를 호출합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CreateProductSyncStrategy implements ProductSyncStrategy {

    private static final Logger log = LoggerFactory.getLogger(CreateProductSyncStrategy.class);

    private final MarketPlaceClient marketPlaceClient;
    private final CreateProductRequestMapper requestMapper;

    public CreateProductSyncStrategy(
            MarketPlaceClient marketPlaceClient, CreateProductRequestMapper requestMapper) {
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
            CreateProductRequest request = requestMapper.toRequest(outbox, product, seller);

            log.info(
                    "[CREATE] 상품 등록 요청 - outboxId={}, itemNo={}, omsSellerId={}",
                    outbox.getId(),
                    outbox.getItemNo(),
                    request.sellerId());

            CreateProductResponse response = marketPlaceClient.createProduct(request);

            log.info(
                    "[CREATE] 상품 등록 성공 - inboundProductId={}, status={}",
                    response.inboundProductId(),
                    response.status());

            return ProductSyncResult.success(response.inboundProductId());
        } catch (JsonProcessingException e) {
            log.error("[CREATE] 페이로드 직렬화 실패 - outboxId={}", outbox.getId(), e);
            return ProductSyncResult.failure("SERIALIZATION_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("[CREATE] 상품 등록 실패 - outboxId={}", outbox.getId(), e);
            return ProductSyncResult.failure("CREATE_FAILED", e.getMessage());
        }
    }
}
