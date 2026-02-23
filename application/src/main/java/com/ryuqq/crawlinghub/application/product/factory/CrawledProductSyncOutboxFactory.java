package com.ryuqq.crawlinghub.application.product.factory;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 외부 동기화 Outbox 생성 Factory
 *
 * <p>CrawledProduct를 기반으로 CrawledProductSyncOutbox를 생성합니다. 변경 유형별 다건 Outbox 생성을 담당하는 순수 팩토리입니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledProduct 상태 기반 동기화 타입 결정 (CREATE / UPDATE_*)
 *   <li>변경 유형별 CrawledProductSyncOutbox 다건 생성
 * </ul>
 *
 * <p><strong>중복 체크는 호출자의 책임</strong>입니다. 이 팩토리는 중복 검증 없이 Outbox를 생성합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductSyncOutboxFactory {

    /**
     * CrawledProduct 기반 CrawledProductSyncOutbox 다건 생성
     *
     * <p>신규 상품이면 CREATE 1건, 기존 상품이면 변경 유형별 UPDATE_* Outbox를 생성합니다.
     *
     * @param product CrawledProduct (호출자가 동기화 가능 여부와 중복을 사전 검증해야 함)
     * @return 생성된 CrawledProductSyncOutbox 목록
     */
    public List<CrawledProductSyncOutbox> createAll(CrawledProduct product) {
        Instant now = Instant.now();

        if (!product.isRegisteredToExternalServer()) {
            return List.of(createSingle(product, SyncType.CREATE, now));
        }

        return product.getPendingChanges().stream()
                .map(CrawledProductSyncOutboxFactory::mapToSyncType)
                .map(syncType -> createSingle(product, syncType, now))
                .toList();
    }

    private CrawledProductSyncOutbox createSingle(
            CrawledProduct product, SyncType syncType, Instant now) {
        return CrawledProductSyncOutbox.forNew(
                product.getId(),
                product.getSellerId(),
                product.getItemNo(),
                syncType,
                product.getExternalProductId(),
                now);
    }

    private static SyncType mapToSyncType(ProductChangeType changeType) {
        return switch (changeType) {
            case PRICE -> SyncType.UPDATE_PRICE;
            case IMAGE -> SyncType.UPDATE_IMAGE;
            case DESCRIPTION -> SyncType.UPDATE_DESCRIPTION;
            case OPTION_STOCK -> SyncType.UPDATE_OPTION_STOCK;
            case PRODUCT_INFO -> SyncType.UPDATE_PRODUCT_INFO;
        };
    }
}
