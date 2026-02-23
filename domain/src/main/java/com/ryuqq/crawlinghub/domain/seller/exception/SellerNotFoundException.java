package com.ryuqq.crawlinghub.domain.seller.exception;

import java.util.Map;

/** 존재하지 않는 셀러를 조회했을 때 발생하는 예외입니다. */
public final class SellerNotFoundException extends SellerException {

    private static final SellerErrorCode ERROR_CODE = SellerErrorCode.SELLER_NOT_FOUND;

    public SellerNotFoundException(long sellerId) {
        super(
                ERROR_CODE,
                String.format("존재하지 않는 셀러입니다. ID: %d", sellerId),
                Map.of("sellerId", sellerId));
    }
}
