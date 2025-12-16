package com.ryuqq.crawlinghub.application.product.factory;

import com.ryuqq.crawlinghub.application.product.dto.bundle.SyncOutboxBundle;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import java.time.Clock;
import org.springframework.stereotype.Component;

/**
 * 외부 동기화 Outbox 생성 Factory
 *
 * <p>CrawledProduct를 기반으로 SyncOutboxBundle을 생성합니다. CREATE/UPDATE 타입 판단과 Outbox + Event 생성을 담당합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledProduct 상태 기반 동기화 타입 결정 (CREATE/UPDATE)
 *   <li>SyncOutbox 생성
 *   <li>ExternalSyncRequestedEvent 생성
 *   <li>Bundle로 묶어 반환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SyncOutboxFactory {

    private final Clock clock;

    public SyncOutboxFactory(Clock clock) {
        this.clock = clock;
    }

    /**
     * CrawledProduct 기반 SyncOutboxBundle 생성
     *
     * <p>CrawledProduct의 externalProductId 존재 여부로 CREATE/UPDATE를 판단합니다.
     *
     * @param product CrawledProduct (동기화 준비 완료된 상태여야 함)
     * @return Outbox와 Event가 포함된 Bundle
     * @throws IllegalStateException 동기화 조건이 충족되지 않은 경우
     */
    public SyncOutboxBundle createBundle(CrawledProduct product) {
        validateSyncReady(product);

        CrawledProductSyncOutbox outbox = createOutbox(product);
        ExternalSyncRequestedEvent event = outbox.createSyncRequestedEvent(clock);

        return new SyncOutboxBundle(outbox, event);
    }

    private void validateSyncReady(CrawledProduct product) {
        if (!product.needsExternalSync()) {
            throw new IllegalStateException(
                    "CrawledProduct is not ready for sync: " + product.getId());
        }
    }

    private CrawledProductSyncOutbox createOutbox(CrawledProduct product) {
        if (product.isRegisteredToExternalServer()) {
            return CrawledProductSyncOutbox.forUpdate(
                    product.getId(),
                    product.getSellerId(),
                    product.getItemNo(),
                    product.getExternalProductId(),
                    clock);
        }
        return CrawledProductSyncOutbox.forCreate(
                product.getId(), product.getSellerId(), product.getItemNo(), clock);
    }
}
