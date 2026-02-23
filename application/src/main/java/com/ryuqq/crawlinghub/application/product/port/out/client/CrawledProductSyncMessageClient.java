package com.ryuqq.crawlinghub.application.product.port.out.client;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;

/**
 * CrawledProductSync 메시지 발행 Port (Port Out - Messaging)
 *
 * <p>SQS 메시지 발행을 위한 추상화 인터페이스
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledProductSyncMessageClient {

    /**
     * CrawledProductSyncOutbox 기반 메시지 발행
     *
     * @param outbox 발행할 CrawledProductSyncOutbox
     */
    void publish(CrawledProductSyncOutbox outbox);
}
