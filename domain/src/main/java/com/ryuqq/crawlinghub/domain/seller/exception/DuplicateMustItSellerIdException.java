package com.ryuqq.crawlinghub.domain.seller.exception;

import java.util.Map;

/** 이미 등록된 머스트잇 셀러 ID가 존재할 때 발생하는 예외입니다. */
public final class DuplicateMustItSellerIdException extends SellerException {

    private static final SellerErrorCode ERROR_CODE = SellerErrorCode.DUPLICATE_MUST_IT_SELLER_ID;

    public DuplicateMustItSellerIdException(String mustItSellerName) {
        super(
                ERROR_CODE,
                String.format("이미 존재하는 머스트잇 셀러 ID입니다: %s", mustItSellerName),
                Map.of("mustItSellerName", mustItSellerName));
    }
}
