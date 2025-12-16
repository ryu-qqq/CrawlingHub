package com.ryuqq.crawlinghub.application.product.dto.bundle;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import java.util.Objects;

/**
 * 외부 서버 동기화 Outbox Bundle
 *
 * <p>SyncOutbox와 Event를 함께 묶어 처리하는 Bundle입니다. Factory에서 생성하여 Manager에서 영속화합니다.
 *
 * <p><strong>사용 흐름</strong>:
 *
 * <pre>
 * 1. Factory에서 CrawledProduct 기반으로 SyncOutboxBundle 생성
 * 2. Manager에서 Outbox 영속화
 * 3. Service에서 Event를 TransactionEventRegistry에 등록
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public record SyncOutboxBundle(CrawledProductSyncOutbox outbox, ExternalSyncRequestedEvent event) {

    public SyncOutboxBundle {
        Objects.requireNonNull(outbox, "outbox must not be null");
        Objects.requireNonNull(event, "event must not be null");
    }

    /**
     * CREATE 타입 요청인지 확인
     *
     * @return CREATE 요청이면 true
     */
    public boolean isCreateRequest() {
        return outbox.isCreateRequest();
    }

    /**
     * UPDATE 타입 요청인지 확인
     *
     * @return UPDATE 요청이면 true
     */
    public boolean isUpdateRequest() {
        return outbox.isUpdateRequest();
    }
}
