package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.ErrorCode;

/**
 * Seller Bounded Context에서 사용하는 ErrorCode Enum입니다.
 */
public enum SellerErrorCode implements ErrorCode {

    DUPLICATE_MUST_IT_SELLER_ID("SELLER-001", 409, "이미 등록된 머스트잇 셀러 ID입니다."),
    DUPLICATE_SELLER_NAME("SELLER-002", 409, "이미 등록된 셀러 이름입니다."),
    SELLER_HAS_ACTIVE_SCHEDULERS("SELLER-003", 400, "활성 상태의 스케줄러가 존재하여 셀러를 비활성화할 수 없습니다."),
    SELLER_NOT_FOUND("SELLER-004", 404, "존재하지 않는 셀러입니다.");

    private final String code;
    private final int httpStatus;
    private final String message;

    SellerErrorCode(String code, int httpStatus, String message) {
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

