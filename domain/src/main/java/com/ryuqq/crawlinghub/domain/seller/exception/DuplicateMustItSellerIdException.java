package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 이미 등록된 머스트잇 셀러 ID가 존재할 때 발생하는 예외입니다. */
public final class DuplicateMustItSellerIdException extends DomainException {

    private static final SellerErrorCode ERROR_CODE = SellerErrorCode.DUPLICATE_MUST_IT_SELLER_ID;

    public DuplicateMustItSellerIdException(String mustItSellerName) {
        super(
                ERROR_CODE.getCode(),
                ERROR_CODE.getMessage(),
                Map.of("mustItSellerName", mustItSellerName));
    }
}
