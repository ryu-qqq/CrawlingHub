package com.ryuqq.crawlinghub.application.sync.service.command;

import com.ryuqq.crawlinghub.application.product.facade.SyncCompletionFacade;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient;
import com.ryuqq.crawlinghub.application.sync.dto.messaging.ProductSyncPayload;
import com.ryuqq.crawlinghub.application.sync.manager.command.SyncOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.sync.manager.query.SyncOutboxReadManager;
import com.ryuqq.crawlinghub.application.sync.port.in.command.ProcessProductSyncFromSqsUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SQS 메시지 기반 외부 서버 동기화 처리 Service
 *
 * <p><strong>용도</strong>: SQS Listener에서 수신한 ProductSync 메시지를 처리
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Outbox 조회 및 상태 검증 (이미 처리된 경우 skip)
 *   <li>PROCESSING 상태로 변경
 *   <li>ExternalProductServerClient로 동기화 요청
 *   <li>성공 시: COMPLETED 상태로 변경 + CrawledProduct synced 처리
 *   <li>실패 시: FAILED 상태로 변경
 * </ol>
 *
 * <p><strong>멱등성</strong>: idempotencyKey를 통해 중복 처리 방지
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessProductSyncFromSqsService implements ProcessProductSyncFromSqsUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessProductSyncFromSqsService.class);

    private final SyncOutboxReadManager syncOutboxReadManager;
    private final SyncOutboxTransactionManager syncOutboxTransactionManager;
    private final CrawledProductReadManager crawledProductReadManager;
    private final SyncCompletionFacade syncCompletionFacade;
    private final ExternalProductServerClient externalProductServerClient;

    public ProcessProductSyncFromSqsService(
            SyncOutboxReadManager syncOutboxReadManager,
            SyncOutboxTransactionManager syncOutboxTransactionManager,
            CrawledProductReadManager crawledProductReadManager,
            SyncCompletionFacade syncCompletionFacade,
            ExternalProductServerClient externalProductServerClient) {
        this.syncOutboxReadManager = syncOutboxReadManager;
        this.syncOutboxTransactionManager = syncOutboxTransactionManager;
        this.crawledProductReadManager = crawledProductReadManager;
        this.syncCompletionFacade = syncCompletionFacade;
        this.externalProductServerClient = externalProductServerClient;
    }

    @Override
    public boolean execute(ProductSyncPayload payload) {
        log.debug(
                "SQS 외부 동기화 처리 시작: outboxId={}, productId={}, syncType={}",
                payload.outboxId(),
                payload.crawledProductId(),
                payload.syncType());

        // 1. Outbox 조회
        Optional<CrawledProductSyncOutbox> outboxOpt =
                syncOutboxReadManager.findById(payload.outboxId());
        if (outboxOpt.isEmpty()) {
            log.warn("Outbox를 찾을 수 없음: outboxId={}", payload.outboxId());
            return false;
        }

        CrawledProductSyncOutbox outbox = outboxOpt.get();

        // 2. 상태 검증 (이미 처리 중이거나 완료된 경우 skip)
        if (outbox.getStatus().isProcessing() || outbox.isCompleted()) {
            log.debug(
                    "이미 처리 중이거나 완료됨 (skip): outboxId={}, status={}",
                    outbox.getId(),
                    outbox.getStatus());
            return true; // skip이지만 성공으로 처리 (ACK 필요)
        }

        // 3. CrawledProduct 조회
        Optional<CrawledProduct> productOpt =
                crawledProductReadManager.findById(outbox.getCrawledProductId());
        if (productOpt.isEmpty()) {
            log.warn(
                    "CrawledProduct를 찾을 수 없음: outboxId={}, productId={}",
                    outbox.getId(),
                    outbox.getCrawledProductId().value());
            syncCompletionFacade.failSync(outbox, "CrawledProduct를 찾을 수 없음");
            return false;
        }

        CrawledProduct product = productOpt.get();

        // 4. PROCESSING 상태로 변경
        syncOutboxTransactionManager.markAsProcessing(outbox);

        // 5. ExternalProductServer API 호출
        try {
            ProductSyncResult result = callExternalApi(outbox, product);

            // 6. 결과 처리
            if (result.success()) {
                syncCompletionFacade.completeSync(outbox, product, result.externalProductId());
                log.info(
                        "SQS 외부 동기화 성공: outboxId={}, productId={}, externalProductId={}",
                        outbox.getId(),
                        product.getIdValue(),
                        result.externalProductId());
                return true;
            } else {
                String errorMessage =
                        String.format(
                                "API 호출 실패: errorCode=%s, errorMessage=%s",
                                result.errorCode(), result.errorMessage());
                syncCompletionFacade.failSync(outbox, errorMessage);
                log.warn("SQS 외부 동기화 실패: outboxId={}, error={}", outbox.getId(), errorMessage);
                return false;
            }
        } catch (Exception e) {
            syncCompletionFacade.failSync(outbox, "API 호출 예외: " + e.getMessage());
            log.error("SQS 외부 동기화 처리 중 예외: outboxId={}, error={}", outbox.getId(), e.getMessage());
            return false;
        }
    }

    private ProductSyncResult callExternalApi(
            CrawledProductSyncOutbox outbox, CrawledProduct product) {
        if (outbox.isCreateRequest()) {
            return externalProductServerClient.createProduct(outbox, product);
        } else {
            return externalProductServerClient.updateProduct(outbox, product);
        }
    }
}
