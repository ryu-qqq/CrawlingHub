package com.ryuqq.crawlinghub.application.sync.service.command;

import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.product.factory.SyncOutboxFactory;
import com.ryuqq.crawlinghub.application.sync.manager.command.SyncOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.sync.port.in.command.RequestSyncUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import org.springframework.stereotype.Service;

/**
 * 외부 서버 동기화 요청 Service
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledProduct 동기화 준비 상태 검증
 *   <li>SyncOutboxBundle 생성 (Factory 위임)
 *   <li>Outbox 영속화 (Manager 위임)
 *   <li>동기화 요청 Event 등록 (트랜잭션 커밋 후 발행)
 * </ul>
 *
 * <p><strong>Bundle 패턴</strong>:
 *
 * <ul>
 *   <li>Factory: CrawledProduct → SyncOutboxBundle (Outbox + Event)
 *   <li>Manager: Bundle의 Outbox 영속화
 *   <li>Service: Bundle의 Event를 TransactionEventRegistry에 등록
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RequestSyncService implements RequestSyncUseCase {

    private final SyncOutboxFactory syncOutboxFactory;
    private final SyncOutboxTransactionManager syncOutboxTransactionManager;
    private final TransactionEventRegistry eventRegistry;

    public RequestSyncService(
            SyncOutboxFactory syncOutboxFactory,
            SyncOutboxTransactionManager syncOutboxTransactionManager,
            TransactionEventRegistry eventRegistry) {
        this.syncOutboxFactory = syncOutboxFactory;
        this.syncOutboxTransactionManager = syncOutboxTransactionManager;
        this.eventRegistry = eventRegistry;
    }

    @Override
    public void requestIfReady(CrawledProduct product) {
        if (!product.needsExternalSync()) {
            return;
        }

        syncOutboxFactory
                .createBundle(product)
                .ifPresent(
                        bundle -> {
                            syncOutboxTransactionManager.persist(bundle);
                            eventRegistry.registerForPublish(bundle.event());
                        });
    }
}
