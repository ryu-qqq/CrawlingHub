package com.ryuqq.crawlinghub.application.sync.service.command;

import com.ryuqq.crawlinghub.application.product.facade.SyncCompletionFacade;
import com.ryuqq.crawlinghub.application.product.manager.SyncOutboxManager;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient;
import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient.ProductSyncResult;
import com.ryuqq.crawlinghub.application.sync.dto.command.SyncRetryResult;
import com.ryuqq.crawlinghub.application.sync.manager.SyncOutboxReadManager;
import com.ryuqq.crawlinghub.application.sync.port.in.command.RetrySyncUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 외부 서버 동기화 재시도 Service
 *
 * <p>실패한 외부 서버 동기화를 재시도합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>ReadManager로 재시도 가능한 Outbox 조회
 *   <li>ExternalProductServerClient로 동기화 재요청
 *   <li>성공 시: COMPLETED 상태로 변경 + CrawledProduct synced 처리
 *   <li>실패 시: FAILED 상태로 변경 (재시도 횟수 증가)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RetrySyncService implements RetrySyncUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetrySyncService.class);
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 3;

    private final SyncOutboxReadManager syncOutboxReadManager;
    private final SyncOutboxManager syncOutboxManager;
    private final CrawledProductReadManager crawledProductReadManager;
    private final SyncCompletionFacade syncCompletionFacade;
    private final ExternalProductServerClient externalProductServerClient;

    public RetrySyncService(
            SyncOutboxReadManager syncOutboxReadManager,
            SyncOutboxManager syncOutboxManager,
            CrawledProductReadManager crawledProductReadManager,
            SyncCompletionFacade syncCompletionFacade,
            ExternalProductServerClient externalProductServerClient) {
        this.syncOutboxReadManager = syncOutboxReadManager;
        this.syncOutboxManager = syncOutboxManager;
        this.crawledProductReadManager = crawledProductReadManager;
        this.syncCompletionFacade = syncCompletionFacade;
        this.externalProductServerClient = externalProductServerClient;
    }

    @Override
    public SyncRetryResult execute() {
        List<CrawledProductSyncOutbox> retryableOutboxes =
                syncOutboxReadManager.findRetryableOutboxes(MAX_RETRY_COUNT, BATCH_SIZE);

        if (retryableOutboxes.isEmpty()) {
            return SyncRetryResult.empty();
        }

        log.info("외부 서버 동기화 재시도 시작: {} 건", retryableOutboxes.size());

        int succeeded = 0;
        int failed = 0;

        for (CrawledProductSyncOutbox outbox : retryableOutboxes) {
            try {
                processOutbox(outbox);
                succeeded++;
            } catch (Exception e) {
                failed++;
                log.warn("외부 서버 동기화 재시도 실패: outboxId={}, error={}", outbox.getId(), e.getMessage());
            }
        }

        boolean hasMore = retryableOutboxes.size() >= BATCH_SIZE;
        return SyncRetryResult.of(retryableOutboxes.size(), succeeded, failed, hasMore);
    }

    private void processOutbox(CrawledProductSyncOutbox outbox) {
        Optional<CrawledProduct> productOpt =
                crawledProductReadManager.findById(outbox.getCrawledProductId());

        if (productOpt.isEmpty()) {
            syncCompletionFacade.failSync(outbox, "CrawledProduct를 찾을 수 없습니다 (재시도)");
            log.warn(
                    "CrawledProduct를 찾을 수 없습니다: productId={}, outboxId={}",
                    outbox.getCrawledProductId().value(),
                    outbox.getId());
            throw new RuntimeException("CrawledProduct를 찾을 수 없습니다");
        }

        CrawledProduct product = productOpt.get();

        syncOutboxManager.markAsProcessing(outbox);

        ProductSyncResult result = callExternalApi(outbox, product);

        handleSyncResult(outbox, product, result);
    }

    private ProductSyncResult callExternalApi(
            CrawledProductSyncOutbox outbox, CrawledProduct product) {
        if (outbox.isCreateRequest()) {
            return externalProductServerClient.createProduct(outbox, product);
        } else {
            return externalProductServerClient.updateProduct(outbox, product);
        }
    }

    private void handleSyncResult(
            CrawledProductSyncOutbox outbox, CrawledProduct product, ProductSyncResult result) {
        if (result.success()) {
            syncCompletionFacade.completeSync(outbox, product, result.externalProductId());
            log.debug("외부 서버 동기화 재시도 성공: outboxId={}", outbox.getId());
        } else {
            String errorMessage =
                    String.format(
                            "API 호출 실패 (재시도): errorCode=%s, errorMessage=%s",
                            result.errorCode(), result.errorMessage());
            syncCompletionFacade.failSync(outbox, errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }
}
