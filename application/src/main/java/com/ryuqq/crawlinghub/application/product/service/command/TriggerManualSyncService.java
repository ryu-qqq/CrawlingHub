package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.product.dto.command.TriggerManualSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.ManualSyncTriggerResponse;
import com.ryuqq.crawlinghub.application.product.manager.SyncOutboxManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.TriggerManualSyncUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.exception.CrawledProductNotFoundException;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.time.Clock;
import org.springframework.stereotype.Service;

/**
 * 수동 동기화 트리거 서비스
 *
 * <p>CrawledProduct에 대해 수동으로 동기화를 트리거합니다.
 */
@Service
public class TriggerManualSyncService implements TriggerManualSyncUseCase {

    private final CrawledProductQueryPort crawledProductQueryPort;
    private final SyncOutboxManager syncOutboxManager;
    private final Clock clock;

    public TriggerManualSyncService(
            CrawledProductQueryPort crawledProductQueryPort,
            SyncOutboxManager syncOutboxManager,
            Clock clock) {
        this.crawledProductQueryPort = crawledProductQueryPort;
        this.syncOutboxManager = syncOutboxManager;
        this.clock = clock;
    }

    @Override
    public ManualSyncTriggerResponse execute(TriggerManualSyncCommand command) {
        CrawledProductId crawledProductId = CrawledProductId.of(command.crawledProductId());
        CrawledProduct product = findProductOrThrow(crawledProductId);

        validateCanSync(product);

        CrawledProductSyncOutbox outbox = createOutbox(product);
        syncOutboxManager.persist(outbox);

        return ManualSyncTriggerResponse.success(
                command.crawledProductId(), outbox.getId(), outbox.getSyncType().name());
    }

    private CrawledProduct findProductOrThrow(CrawledProductId crawledProductId) {
        return crawledProductQueryPort
                .findById(crawledProductId)
                .orElseThrow(() -> new CrawledProductNotFoundException(crawledProductId.value()));
    }

    private void validateCanSync(CrawledProduct product) {
        if (!product.canSyncToExternalServer()) {
            throw new IllegalStateException("동기화할 수 없는 상품입니다. 모든 크롤링이 완료되고 이미지가 업로드되어야 합니다.");
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
