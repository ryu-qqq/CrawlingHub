package com.ryuqq.crawlinghub.application.product.manager;

import com.ryuqq.crawlinghub.application.product.port.out.client.CrawledProductSyncMessageClient;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawledProductSync 메시지 관리자
 *
 * <p><strong>책임</strong>: CrawledProductSyncOutbox SQS 메시지 발행 관리
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductSyncMessageManager {

    private static final Logger log =
            LoggerFactory.getLogger(CrawledProductSyncMessageManager.class);

    private final CrawledProductSyncMessageClient messageClient;

    public CrawledProductSyncMessageManager(CrawledProductSyncMessageClient messageClient) {
        this.messageClient = messageClient;
    }

    /**
     * CrawledProductSyncOutbox 메시지 발행
     *
     * @param outbox 발행할 CrawledProductSyncOutbox
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publish(CrawledProductSyncOutbox outbox) {
        log.debug(
                "CrawledProductSync 메시지 발행 시작: outboxId={}, crawledProductId={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue());

        messageClient.publish(outbox);

        log.info(
                "CrawledProductSync 메시지 발행 완료: outboxId={}, crawledProductId={}",
                outbox.getId(),
                outbox.getCrawledProductIdValue());
    }
}
