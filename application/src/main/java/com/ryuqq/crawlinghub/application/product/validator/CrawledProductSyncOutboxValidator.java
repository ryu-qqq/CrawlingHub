package com.ryuqq.crawlinghub.application.product.validator;

import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * CrawledProductSyncOutbox 중복 검증기
 *
 * <p>동일 상품에 대해 활성(PENDING/SENT/PROCESSING) 상태의 Outbox가 이미 존재하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductSyncOutboxValidator {

    private final CrawledProductSyncOutboxReadManager syncOutboxReadManager;

    public CrawledProductSyncOutboxValidator(
            CrawledProductSyncOutboxReadManager syncOutboxReadManager) {
        this.syncOutboxReadManager = syncOutboxReadManager;
    }

    /**
     * 활성 Outbox가 이미 존재하는 SyncType 필터링
     *
     * @param productId 대상 CrawledProduct ID
     * @param syncTypes 확인할 SyncType 목록
     * @return 이미 활성 Outbox가 있는 SyncType Set
     */
    public Set<SyncType> filterAlreadyActive(CrawledProductId productId, List<SyncType> syncTypes) {
        return syncTypes.stream()
                .filter(syncType -> syncOutboxReadManager.existsActiveOutbox(productId, syncType))
                .collect(Collectors.toSet());
    }
}
