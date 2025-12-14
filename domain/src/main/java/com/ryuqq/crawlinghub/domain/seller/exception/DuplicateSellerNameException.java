package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 이미 등록된 셀러 이름이 존재할 때 발생하는 예외입니다. */
public final class DuplicateSellerNameException extends DomainException {

    private static final SellerErrorCode ERROR_CODE = SellerErrorCode.DUPLICATE_SELLER_NAME;

    public DuplicateSellerNameException(String sellerName) {
        super(
                ERROR_CODE,
                String.format("이미 존재하는 셀러 이름입니다: %s", sellerName),
                Map.of("sellerName", sellerName));
    }
}
