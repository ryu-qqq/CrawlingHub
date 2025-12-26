package com.ryuqq.crawlinghub.domain.product.exception;

import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;

/** CrawledProduct Bounded Context에서 사용하는 ErrorCode Enum입니다. */
public enum CrawledProductErrorCode implements ErrorCode {
    CRAWLED_PRODUCT_NOT_FOUND("PRODUCT-001", 404, "존재하지 않는 크롤링 상품입니다."),
    SYNC_OUTBOX_NOT_FOUND("OUTBOX-001", 404, "존재하지 않는 SyncOutbox입니다."),
    IMAGE_OUTBOX_NOT_FOUND("OUTBOX-002", 404, "존재하지 않는 ImageOutbox입니다.");

    private final String code;
    private final int httpStatus;
    private final String message;

    CrawledProductErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
