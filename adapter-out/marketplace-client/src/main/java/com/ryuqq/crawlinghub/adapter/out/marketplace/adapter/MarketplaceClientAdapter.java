package com.ryuqq.crawlinghub.adapter.out.marketplace.adapter;

import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Marketplace Client Adapter (Stub Implementation)
 *
 * <p>ExternalProductServerClient Port의 구현체입니다. 외부 상품 서버에 상품 정보를 등록/갱신합니다.
 *
 * <p><strong>현재 상태:</strong> 로그만 남기는 더미 구현입니다. 실제 외부 API 연동 시 구현이 필요합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>상품 신규 등록 (POST /api/v1/products)
 *   <li>상품 정보 갱신 (PUT /api/v1/products/{id})
 *   <li>상품 존재 여부 확인 (GET /api/v1/products/{id}/exists)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class MarketplaceClientAdapter implements ExternalProductServerClient {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceClientAdapter.class);

    // 더미 구현을 위한 외부 상품 ID 생성기
    private final AtomicLong externalProductIdGenerator = new AtomicLong(1_000_000L);

    /**
     * 상품 신규 등록
     *
     * <p>[STUB] 현재는 로그만 남기고 성공 응답을 반환합니다.
     *
     * @param outbox 동기화 Outbox (idempotencyKey 포함)
     * @param product 크롤링된 상품 정보
     * @return 등록 결과 (더미 외부 상품 ID 포함)
     */
    @Override
    public ProductSyncResult createProduct(
            CrawledProductSyncOutbox outbox, CrawledProduct product) {
        Long externalProductId = externalProductIdGenerator.incrementAndGet();

        log.info(
                "[STUB] 상품 신규 등록 요청 - "
                        + "outboxId={}, crawledProductId={}, sellerId={}, itemNo={}, "
                        + "idempotencyKey={}, itemName={}, externalProductId={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getSellerIdValue(),
                outbox.getItemNo(),
                outbox.getIdempotencyKey(),
                product.getItemName(),
                externalProductId);

        log.debug(
                "[STUB] 상품 상세 정보 - " + "brandName={}, price={}, category={}, optionCount={}",
                product.getBrandName(),
                product.getPrice(),
                product.getCategory(),
                product.getOptions() != null ? product.getOptions().size() : 0);

        // TODO: 실제 외부 API 호출 구현
        // 1. CrawledProduct → External API Request DTO 변환
        // 2. WebClient로 POST /api/v1/products 호출
        // 3. 응답에서 externalProductId 추출
        // 4. 실패 시 ProductSyncResult.failure() 반환

        return ProductSyncResult.success(externalProductId);
    }

    /**
     * 상품 정보 갱신
     *
     * <p>[STUB] 현재는 로그만 남기고 성공 응답을 반환합니다.
     *
     * @param outbox 동기화 Outbox (externalProductId, idempotencyKey 포함)
     * @param product 크롤링된 상품 정보
     * @return 갱신 결과
     */
    @Override
    public ProductSyncResult updateProduct(
            CrawledProductSyncOutbox outbox, CrawledProduct product) {
        Long externalProductId = outbox.getExternalProductId();

        log.info(
                "[STUB] 상품 정보 갱신 요청 - "
                        + "outboxId={}, crawledProductId={}, sellerId={}, itemNo={}, "
                        + "idempotencyKey={}, externalProductId={}, itemName={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue(),
                outbox.getSellerIdValue(),
                outbox.getItemNo(),
                outbox.getIdempotencyKey(),
                externalProductId,
                product.getItemName());

        log.debug(
                "[STUB] 갱신 상세 정보 - " + "brandName={}, price={}, category={}, optionCount={}",
                product.getBrandName(),
                product.getPrice(),
                product.getCategory(),
                product.getOptions() != null ? product.getOptions().size() : 0);

        // TODO: 실제 외부 API 호출 구현
        // 1. CrawledProduct → External API Request DTO 변환
        // 2. WebClient로 PUT /api/v1/products/{externalProductId} 호출
        // 3. 실패 시 ProductSyncResult.failure() 반환

        return ProductSyncResult.success(externalProductId);
    }

    /**
     * 상품 존재 여부 확인
     *
     * <p>[STUB] 현재는 로그만 남기고 true를 반환합니다.
     *
     * @param externalProductId 외부 상품 ID
     * @return 존재하면 true (현재는 항상 true)
     */
    @Override
    public boolean existsProduct(Long externalProductId) {
        log.info("[STUB] 상품 존재 여부 확인 - externalProductId={}", externalProductId);

        // TODO: 실제 외부 API 호출 구현
        // 1. WebClient로 GET /api/v1/products/{externalProductId}/exists 호출
        // 2. 응답에 따라 true/false 반환

        return true;
    }
}
