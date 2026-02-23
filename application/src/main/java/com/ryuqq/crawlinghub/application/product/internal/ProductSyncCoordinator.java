package com.ryuqq.crawlinghub.application.product.internal;

import com.ryuqq.crawlinghub.application.product.dto.command.ProcessProductSyncCommand;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductCommandManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.application.product.port.out.client.ExternalProductServerClient;
import com.ryuqq.crawlinghub.application.product.validator.ProductSyncValidator;
import com.ryuqq.crawlinghub.application.product.validator.ProductSyncValidator.SyncTarget;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncResult;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SQS 수신 후 전체 동기화 흐름 Coordinator
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Validator로 Outbox + Product 통합 검증
 *   <li>PROCESSING 상태 전환
 *   <li>외부 API 호출 (Port에 위임)
 *   <li>성공/실패 처리
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductSyncCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncCoordinator.class);

    private final ProductSyncValidator validator;
    private final CrawledProductSyncOutboxCommandManager syncOutboxCommandManager;
    private final CrawledProductCommandManager crawledProductCommandManager;
    private final ExternalProductServerClient externalProductServerClient;

    public ProductSyncCoordinator(
            ProductSyncValidator validator,
            CrawledProductSyncOutboxCommandManager syncOutboxCommandManager,
            CrawledProductCommandManager crawledProductCommandManager,
            ExternalProductServerClient externalProductServerClient) {
        this.validator = validator;
        this.syncOutboxCommandManager = syncOutboxCommandManager;
        this.crawledProductCommandManager = crawledProductCommandManager;
        this.externalProductServerClient = externalProductServerClient;
    }

    /**
     * 전체 동기화 흐름 조율
     *
     * @param command SQS 수신 Command
     * @return 처리 성공 여부
     */
    public boolean processSyncRequest(ProcessProductSyncCommand command) {
        log.debug(
                "SQS 외부 동기화 처리 시작: outboxId={}, productId={}, syncType={}",
                command.outboxId(),
                command.crawledProductId(),
                command.syncType());

        // 1. 검증 (Outbox + Product 통합)
        Optional<SyncTarget> targetOpt = validator.validateAndResolve(command.outboxId());
        if (targetOpt.isEmpty()) {
            return false;
        }

        SyncTarget target = targetOpt.get();

        // 2. PROCESSING 상태 전환
        syncOutboxCommandManager.markAsProcessing(target.outbox());

        // 3. 외부 API 호출 + 결과 처리
        try {
            ProductSyncResult result =
                    externalProductServerClient.sync(target.outbox(), target.product());

            if (result.success()) {
                completeSync(target.outbox(), target.product(), result.externalProductId());
                log.info(
                        "SQS 외부 동기화 성공: outboxId={}, productId={}, externalProductId={}",
                        target.outbox().getId(),
                        target.product().getIdValue(),
                        result.externalProductId());
                return true;
            } else {
                failSync(target.outbox(), result.toErrorMessage());
                log.warn(
                        "SQS 외부 동기화 실패: outboxId={}, error={}",
                        target.outbox().getId(),
                        result.toErrorMessage());
                return false;
            }
        } catch (Exception e) {
            failSync(target.outbox(), "API 호출 예외: " + e.getMessage());
            log.error(
                    "SQS 외부 동기화 처리 중 예외: outboxId={}, error={}",
                    target.outbox().getId(),
                    e.getMessage());
            return false;
        }
    }

    // === Private Methods ===

    private void completeSync(
            CrawledProductSyncOutbox outbox, CrawledProduct product, Long externalProductId) {
        syncOutboxCommandManager.markAsCompleted(outbox, externalProductId);

        Instant now = Instant.now();
        if (outbox.getSyncType().isCreate()) {
            product.markAsSynced(externalProductId, now);
        } else {
            product.markChangesSynced(Set.of(outbox.getSyncType().toChangeType()), now);
        }
        crawledProductCommandManager.persist(product);
    }

    private void failSync(CrawledProductSyncOutbox outbox, String errorMessage) {
        syncOutboxCommandManager.markAsFailed(outbox, errorMessage);
    }
}
