package com.ryuqq.crawlinghub.application.product.facade;

import com.ryuqq.crawlinghub.application.product.manager.SyncOutboxManager;
import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부 서버 동기화 완료 처리 Facade
 *
 * <p>SyncOutboxManager와 CrawledProductTransactionManager를 조합하여 동기화 완료/실패 처리를 담당합니다.
 *
 * <p><strong>처리 로직</strong>:
 *
 * <ul>
 *   <li>성공 시: Outbox COMPLETED + CrawledProduct synced 처리
 *   <li>실패 시: Outbox FAILED 처리 (재시도 스케줄러에서 처리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SyncCompletionFacade {

    private static final Logger log = LoggerFactory.getLogger(SyncCompletionFacade.class);

    private final SyncOutboxManager syncOutboxManager;
    private final CrawledProductTransactionManager crawledProductManager;

    public SyncCompletionFacade(
            SyncOutboxManager syncOutboxManager,
            CrawledProductTransactionManager crawledProductManager) {
        this.syncOutboxManager = syncOutboxManager;
        this.crawledProductManager = crawledProductManager;
    }

    /**
     * 동기화 성공 처리
     *
     * <p>Outbox를 COMPLETED 상태로, CrawledProduct를 synced 상태로 변경합니다.
     *
     * @param outbox 동기화 Outbox
     * @param product 크롤링된 상품
     * @param externalProductId 외부 서버에서 발급된 상품 ID
     */
    @Transactional
    public void completeSync(
            CrawledProductSyncOutbox outbox, CrawledProduct product, Long externalProductId) {
        syncOutboxManager.markAsCompleted(outbox, externalProductId);

        // CREATE인 경우 CrawledProduct에 externalProductId 저장
        if (outbox.isCreateRequest()) {
            crawledProductManager.markAsSynced(product, externalProductId);
        } else {
            crawledProductManager.markAsSynced(product, product.getExternalProductId());
        }

        log.info(
                "동기화 완료 처리: outboxId={}, productId={}, externalProductId={}",
                outbox.getId(),
                product.getIdValue(),
                externalProductId);
    }

    /**
     * 동기화 실패 처리
     *
     * <p>Outbox를 FAILED 상태로 변경합니다. 재시도 스케줄러에서 재처리됩니다.
     *
     * @param outbox 동기화 Outbox
     * @param errorMessage 실패 사유
     */
    @Transactional
    public void failSync(CrawledProductSyncOutbox outbox, String errorMessage) {
        syncOutboxManager.markAsFailed(outbox, errorMessage);

        log.warn(
                "동기화 실패 처리: outboxId={}, productId={}, error={}",
                outbox.getId(),
                outbox.getCrawledProductId().value(),
                errorMessage);
    }
}
