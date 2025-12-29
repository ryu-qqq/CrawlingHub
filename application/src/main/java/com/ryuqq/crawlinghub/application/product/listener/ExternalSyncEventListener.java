package com.ryuqq.crawlinghub.application.product.listener;

import com.ryuqq.crawlinghub.application.product.facade.SyncCompletionFacade;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient;
import com.ryuqq.crawlinghub.application.sync.manager.command.SyncOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.sync.manager.query.SyncOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 외부 서버 동기화 요청 이벤트 리스너
 *
 * <p><strong>용도</strong>: 트랜잭션 커밋 후 외부 상품 서버 API 호출 및 상태 업데이트
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후)
 *   <li>SyncOutbox 조회 및 상태 PROCESSING으로 변경
 *   <li>CrawledProduct 조회하여 API 요청 구성
 *   <li>외부 상품 서버 API 호출 (신규 등록 또는 갱신)
 *   <li>성공 시: Outbox 상태 → COMPLETED, CrawledProduct 상태 업데이트
 *   <li>실패 시: Outbox 상태 → FAILED (재시도 스케줄러에서 처리)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ExternalSyncEventListener {

    private static final Logger log = LoggerFactory.getLogger(ExternalSyncEventListener.class);

    private final SyncOutboxReadManager syncOutboxReadManager;
    private final SyncOutboxTransactionManager syncOutboxTransactionManager;
    private final CrawledProductReadManager crawledProductReadManager;
    private final SyncCompletionFacade syncCompletionFacade;
    private final ExternalProductServerClient externalProductServerClient;

    public ExternalSyncEventListener(
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

    /**
     * 외부 서버 동기화 요청 이벤트 처리
     *
     * <p>트랜잭션 커밋 후 실행됩니다.
     *
     * @param event 외부 서버 동기화 요청 이벤트
     */
    @EventListener
    public void handleExternalSyncRequested(ExternalSyncRequestedEvent event) {
        CrawledProductId productId = event.crawledProductId();
        String idempotencyKey = event.idempotencyKey();

        log.info(
                "외부 서버 동기화 요청 이벤트 수신: productId={}, syncType={}, idempotencyKey={}",
                productId.value(),
                event.syncType(),
                idempotencyKey);

        Optional<CrawledProductSyncOutbox> outboxOpt =
                syncOutboxReadManager.findByIdempotencyKey(idempotencyKey);

        if (outboxOpt.isEmpty()) {
            log.warn(
                    "SyncOutbox를 찾을 수 없습니다: idempotencyKey={}, productId={}",
                    idempotencyKey,
                    productId.value());
            return;
        }

        CrawledProductSyncOutbox outbox = outboxOpt.get();
        processOutbox(outbox, productId);

        log.info(
                "외부 서버 동기화 요청 이벤트 처리 완료: productId={}, syncType={}",
                productId.value(),
                event.syncType());
    }

    private void processOutbox(CrawledProductSyncOutbox outbox, CrawledProductId productId) {
        try {
            // 1. CrawledProduct 조회
            Optional<CrawledProduct> productOpt = crawledProductReadManager.findById(productId);
            if (productOpt.isEmpty()) {
                syncCompletionFacade.failSync(outbox, "CrawledProduct를 찾을 수 없습니다");
                log.warn(
                        "CrawledProduct를 찾을 수 없습니다: productId={}, outboxId={}",
                        productId.value(),
                        outbox.getId());
                return;
            }

            CrawledProduct product = productOpt.get();

            // 2. 상태 → PROCESSING
            syncOutboxTransactionManager.markAsProcessing(outbox);

            // 3. 외부 API 호출
            ProductSyncResult result = callExternalApi(outbox, product);

            // 4. 결과 처리
            handleSyncResult(outbox, product, result);

        } catch (Exception e) {
            // 예외 발생 → FAILED
            syncCompletionFacade.failSync(outbox, e.getMessage());
            log.error(
                    "외부 서버 동기화 처리 중 오류: outboxId={}, productId={}, error={}",
                    outbox.getId(),
                    productId.value(),
                    e.getMessage(),
                    e);
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

    private void handleSyncResult(
            CrawledProductSyncOutbox outbox, CrawledProduct product, ProductSyncResult result) {
        if (result.success()) {
            syncCompletionFacade.completeSync(outbox, product, result.externalProductId());
        } else {
            String errorMessage =
                    String.format(
                            "API 호출 실패: errorCode=%s, errorMessage=%s",
                            result.errorCode(), result.errorMessage());
            syncCompletionFacade.failSync(outbox, errorMessage);
        }
    }
}
