package com.ryuqq.crawlinghub.domain.product.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 존재하지 않는 SyncOutbox를 조회했을 때 발생하는 예외입니다. */
public final class SyncOutboxNotFoundException extends DomainException {

    private static final CrawledProductErrorCode ERROR_CODE =
            CrawledProductErrorCode.SYNC_OUTBOX_NOT_FOUND;

    public SyncOutboxNotFoundException(long outboxId) {
        super(
                ERROR_CODE,
                String.format("존재하지 않는 SyncOutbox입니다. ID: %d", outboxId),
                Map.of("outboxId", outboxId));
    }
}
