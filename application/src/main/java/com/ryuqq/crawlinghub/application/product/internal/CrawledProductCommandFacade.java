package com.ryuqq.crawlinghub.application.product.internal;

import com.ryuqq.crawlinghub.application.product.manager.CrawledProductCommandManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledProduct Command Facade
 *
 * <p><strong>책임</strong>: CrawledProduct + SyncOutbox의 persist를 @Transactional 경계 내에서 원자적으로 관리
 *
 * <p><strong>트랜잭션 원칙</strong>: Coordinator는 도메인 상태만 변경하고, 변경된 도메인 객체를 이 Facade에 넘겨 트랜잭션으로 묶어 저장합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductCommandFacade {

    private final CrawledProductCommandManager commandManager;
    private final CrawledProductReadManager readManager;
    private final CrawledProductSyncOutboxCoordinator syncOutboxCoordinator;
    private final CrawledProductSyncOutboxCommandManager syncOutboxCommandManager;

    public CrawledProductCommandFacade(
            CrawledProductCommandManager commandManager,
            CrawledProductReadManager readManager,
            CrawledProductSyncOutboxCoordinator syncOutboxCoordinator,
            CrawledProductSyncOutboxCommandManager syncOutboxCommandManager) {
        this.commandManager = commandManager;
        this.readManager = readManager;
        this.syncOutboxCoordinator = syncOutboxCoordinator;
        this.syncOutboxCommandManager = syncOutboxCommandManager;
    }

    /**
     * product 저장 + outbox 생성 (Raw 처리용)
     *
     * <p>트랜잭션 내에서 수행:
     *
     * <ol>
     *   <li>CrawledProduct 저장
     *   <li>외부 동기화 필요 시 SyncOutbox 생성
     * </ol>
     *
     * @param product 저장할 CrawledProduct
     * @return 저장된 CrawledProduct ID
     */
    @Transactional
    public CrawledProductId persistAndSync(CrawledProduct product) {
        CrawledProductId id = commandManager.persist(product);
        if (product.needsExternalSync()) {
            syncOutboxCoordinator.createAllIfAbsent(product);
        }
        return id;
    }

    /**
     * sync 완료 처리 (outbox 완료 + product 상태 클리어)
     *
     * <p>트랜잭션 내에서 수행:
     *
     * <ol>
     *   <li>SyncOutbox 완료 처리
     *   <li>CrawledProduct를 DB에서 최신 버전으로 재조회
     *   <li>CrawledProduct 동기화 상태 갱신
     *   <li>CrawledProduct 저장
     * </ol>
     *
     * <p><strong>Product 재조회 이유</strong>: Coordinator에서 전달된 Product 객체는 외부 API 호출 전에 로드된 것으로, 호출 중
     * 다른 트랜잭션이 같은 Product를 수정하면 @Version 충돌(OptimisticLockException)이 발생합니다. 트랜잭션 내에서 최신 버전을 재조회하여
     * 안전하게 저장합니다.
     *
     * @param outbox 완료할 SyncOutbox
     * @param externalProductId 외부 상품 ID
     */
    @Transactional
    public void completeSyncAndPersist(CrawledProductSyncOutbox outbox, Long externalProductId) {
        syncOutboxCommandManager.markAsCompleted(outbox, externalProductId);

        CrawledProduct freshProduct =
                readManager
                        .findById(outbox.getCrawledProductId())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "CrawledProduct를 찾을 수 없음: productId="
                                                        + outbox.getCrawledProductIdValue()));

        Instant now = Instant.now();
        if (outbox.getSyncType().isCreate()) {
            freshProduct.markAsSynced(externalProductId, now);
        } else {
            freshProduct.markChangesSynced(Set.of(outbox.getSyncType().toChangeType()), now);
        }
        commandManager.persist(freshProduct);
    }
}
