package com.ryuqq.crawlinghub.application.product.internal;

import com.ryuqq.crawlinghub.application.product.factory.CrawledProductSyncOutboxFactory;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.application.product.validator.CrawledProductSyncOutboxValidator;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawledProductSyncOutbox 생성 Coordinator
 *
 * <p>중복 검증(Validator) → 생성(Factory) → 영속화(CommandManager)를 오케스트레이션합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductSyncOutboxCoordinator {

    private static final Logger log =
            LoggerFactory.getLogger(CrawledProductSyncOutboxCoordinator.class);

    private final CrawledProductSyncOutboxValidator validator;
    private final CrawledProductSyncOutboxFactory syncOutboxFactory;
    private final CrawledProductSyncOutboxCommandManager syncOutboxCommandManager;

    public CrawledProductSyncOutboxCoordinator(
            CrawledProductSyncOutboxValidator validator,
            CrawledProductSyncOutboxFactory syncOutboxFactory,
            CrawledProductSyncOutboxCommandManager syncOutboxCommandManager) {
        this.validator = validator;
        this.syncOutboxFactory = syncOutboxFactory;
        this.syncOutboxCommandManager = syncOutboxCommandManager;
    }

    /**
     * 활성 Outbox가 없는 변경 유형에 대해 Outbox 생성 후 영속화
     *
     * @param product 대상 CrawledProduct
     * @return 생성된 Outbox 목록 (모두 활성 Outbox가 이미 존재하면 빈 목록)
     */
    public List<CrawledProductSyncOutbox> createAllIfAbsent(CrawledProduct product) {
        List<CrawledProductSyncOutbox> candidates = syncOutboxFactory.createAll(product);

        if (candidates.isEmpty()) {
            return List.of();
        }

        List<SyncType> candidateSyncTypes =
                candidates.stream().map(CrawledProductSyncOutbox::getSyncType).toList();
        Set<SyncType> alreadyActive =
                validator.filterAlreadyActive(product.getId(), candidateSyncTypes);

        List<CrawledProductSyncOutbox> toCreate =
                candidates.stream()
                        .filter(outbox -> !alreadyActive.contains(outbox.getSyncType()))
                        .toList();

        if (toCreate.isEmpty()) {
            log.debug("모든 SyncType에 활성 Outbox 존재하여 스킵: productId={}", product.getId());
            return List.of();
        }

        toCreate.forEach(syncOutboxCommandManager::persist);
        return toCreate;
    }
}
