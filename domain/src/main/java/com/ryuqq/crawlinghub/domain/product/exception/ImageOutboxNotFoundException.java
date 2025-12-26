package com.ryuqq.crawlinghub.domain.product.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 존재하지 않는 ImageOutbox를 조회했을 때 발생하는 예외입니다. */
public final class ImageOutboxNotFoundException extends DomainException {

    private static final CrawledProductErrorCode ERROR_CODE =
            CrawledProductErrorCode.IMAGE_OUTBOX_NOT_FOUND;

    public ImageOutboxNotFoundException(long outboxId) {
        super(
                ERROR_CODE,
                String.format("존재하지 않는 ImageOutbox입니다. ID: %d", outboxId),
                Map.of("outboxId", outboxId));
    }
}
