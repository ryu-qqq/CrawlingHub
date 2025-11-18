package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

import java.util.Map;

/**
 * Seller가 이미 존재할 때 발생하는 예외
 */
public class SellerDuplicatedException extends DomainException {

    public SellerDuplicatedException(String message) {
        super(message);
    }

    public SellerDuplicatedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String code() {
        return SellerErrorCode.SELLER_DUPLICATED.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of();
    }
}
