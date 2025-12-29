package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.product.dto.bundle.SyncOutboxBundle;
import com.ryuqq.crawlinghub.application.product.dto.command.TriggerManualSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.ManualSyncTriggerResponse;
import com.ryuqq.crawlinghub.application.product.factory.SyncOutboxFactory;
import com.ryuqq.crawlinghub.application.product.port.in.command.TriggerManualSyncUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.application.sync.manager.command.SyncOutboxTransactionManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.exception.CrawledProductNotFoundException;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import org.springframework.stereotype.Service;

/**
 * 수동 동기화 트리거 서비스
 *
 * <p>CrawledProduct에 대해 수동으로 동기화를 트리거합니다. 중복 PENDING/PROCESSING Outbox가 있는 경우 새 Outbox를 생성하지 않습니다.
 */
@Service
public class TriggerManualSyncService implements TriggerManualSyncUseCase {

    private final CrawledProductQueryPort crawledProductQueryPort;
    private final SyncOutboxFactory syncOutboxFactory;
    private final SyncOutboxTransactionManager syncOutboxTransactionManager;

    public TriggerManualSyncService(
            CrawledProductQueryPort crawledProductQueryPort,
            SyncOutboxFactory syncOutboxFactory,
            SyncOutboxTransactionManager syncOutboxTransactionManager) {
        this.crawledProductQueryPort = crawledProductQueryPort;
        this.syncOutboxFactory = syncOutboxFactory;
        this.syncOutboxTransactionManager = syncOutboxTransactionManager;
    }

    @Override
    public ManualSyncTriggerResponse execute(TriggerManualSyncCommand command) {
        CrawledProductId crawledProductId = CrawledProductId.of(command.crawledProductId());
        CrawledProduct product = findProductOrThrow(crawledProductId);

        validateCanSync(product);

        return syncOutboxFactory
                .createBundle(product)
                .map(bundle -> persistAndReturnSuccess(command.crawledProductId(), bundle))
                .orElseGet(
                        () ->
                                ManualSyncTriggerResponse.skipped(
                                        command.crawledProductId(),
                                        "이미 PENDING 또는 PROCESSING 상태의 동기화 요청이 존재합니다."));
    }

    private ManualSyncTriggerResponse persistAndReturnSuccess(
            Long crawledProductId, SyncOutboxBundle bundle) {
        syncOutboxTransactionManager.persist(bundle);
        return ManualSyncTriggerResponse.success(
                crawledProductId, bundle.outbox().getId(), bundle.outbox().getSyncType().name());
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
}
