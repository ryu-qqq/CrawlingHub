package com.ryuqq.crawlinghub.application.product.port.out.command;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;

/**
 * 외부 동기화 Outbox 저장 Port (Port Out - Command)
 *
 * <p>CrawledProductSyncOutboxManager에서만 사용됩니다.
 *
 * <p>외부 상품 서버 동기화 요청의 트랜잭션 경계를 관리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledProductSyncOutboxPersistencePort {

    /**
     * CrawledProductSyncOutbox 저장
     *
     * @param outbox 저장할 Outbox
     */
    void persist(CrawledProductSyncOutbox outbox);

    /**
     * CrawledProductSyncOutbox 상태 업데이트
     *
     * @param outbox 업데이트할 Outbox
     */
    void update(CrawledProductSyncOutbox outbox);
}
