package com.ryuqq.crawlinghub.domain.product.exception;

import java.util.Map;

/** 존재하지 않는 CrawledProductSyncOutbox를 조회했을 때 발생하는 예외입니다. */
public final class CrawledProductSyncOutboxNotFoundException extends CrawledProductException {

    private static final CrawledProductErrorCode ERROR_CODE =
            CrawledProductErrorCode.SYNC_OUTBOX_NOT_FOUND;

    public CrawledProductSyncOutboxNotFoundException(long outboxId) {
        super(
                ERROR_CODE,
                String.format("존재하지 않는 CrawledProductSyncOutbox입니다. ID: %d", outboxId),
                Map.of("outboxId", outboxId));
    }
}
