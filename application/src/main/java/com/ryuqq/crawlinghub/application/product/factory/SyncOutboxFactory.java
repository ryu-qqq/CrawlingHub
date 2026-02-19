package com.ryuqq.crawlinghub.application.product.factory;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.product.dto.bundle.SyncOutboxBundle;
import com.ryuqq.crawlinghub.application.product.port.out.query.SyncOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import java.time.Instant;
import java.util.Optional;
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

    private final SyncOutboxQueryPort syncOutboxQueryPort;
    private final TimeProvider timeProvider;

    public SyncOutboxFactory(SyncOutboxQueryPort syncOutboxQueryPort, TimeProvider timeProvider) {
        this.syncOutboxQueryPort = syncOutboxQueryPort;
        this.timeProvider = timeProvider;
    }

    /**
     * CrawledProduct 기반 SyncOutboxBundle 생성
     *
     * <p>CrawledProduct의 externalProductId 존재 여부로 CREATE/UPDATE를 판단합니다. 이미 PENDING 또는 PROCESSING
     * 상태의 동일 Outbox가 존재하면 중복 생성을 방지하기 위해 Optional.empty()를 반환합니다.
     *
     * <p><strong>중요</strong>: 호출자가 동기화 가능 여부를 사전에 검증해야 합니다.
     *
     * <ul>
     *   <li>자동 동기화: {@code product.needsExternalSync()} 확인
     *   <li>수동 동기화: {@code product.canSyncToExternalServer()} 확인
     * </ul>
     *
     * @param product CrawledProduct (호출자가 동기화 가능 여부를 사전 검증해야 함)
     * @return Outbox와 Event가 포함된 Bundle (중복인 경우 Optional.empty())
     */
    public Optional<SyncOutboxBundle> createBundle(CrawledProduct product) {
        // 중복 PENDING/PROCESSING Outbox 확인
        CrawledProductSyncOutbox.SyncType syncType = determineSyncType(product);
        String idempotencyKey =
                CrawledProductSyncOutbox.generateIdempotencyKey(product.getId(), syncType);

        Optional<CrawledProductSyncOutbox> existingOutbox =
                syncOutboxQueryPort.findByIdempotencyKey(idempotencyKey);

        if (existingOutbox.isPresent() && isInProgressOrPending(existingOutbox.get())) {
            return Optional.empty();
        }

        CrawledProductSyncOutbox outbox = createOutbox(product, syncType);
        ExternalSyncRequestedEvent event = outbox.createSyncRequestedEvent(timeProvider.now());

        return Optional.of(new SyncOutboxBundle(outbox, event));
    }

    private CrawledProductSyncOutbox.SyncType determineSyncType(CrawledProduct product) {
        return product.isRegisteredToExternalServer()
                ? CrawledProductSyncOutbox.SyncType.UPDATE
                : CrawledProductSyncOutbox.SyncType.CREATE;
    }

    private boolean isInProgressOrPending(CrawledProductSyncOutbox outbox) {
        return outbox.isPending() || outbox.getStatus().isProcessing();
    }

    private CrawledProductSyncOutbox createOutbox(
            CrawledProduct product, CrawledProductSyncOutbox.SyncType syncType) {
        Instant now = timeProvider.now();
        if (syncType == CrawledProductSyncOutbox.SyncType.UPDATE) {
            return CrawledProductSyncOutbox.forUpdate(
                    product.getId(),
                    product.getSellerId(),
                    product.getItemNo(),
                    product.getExternalProductId(),
                    now);
        }
        return CrawledProductSyncOutbox.forCreate(
                product.getId(), product.getSellerId(), product.getItemNo(), now);
    }
}
