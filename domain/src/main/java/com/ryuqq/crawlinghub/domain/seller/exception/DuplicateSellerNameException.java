package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 이미 등록된 셀러 이름이 존재할 때 발생하는 예외입니다. */
public final class DuplicateSellerNameException extends DomainException {

    private static final SellerErrorCode ERROR_CODE = SellerErrorCode.DUPLICATE_SELLER_NAME;

    private final Map<String, Object> args;

    public DuplicateSellerNameException(String sellerName) {
        super(ERROR_CODE.getMessage());
        this.args = Map.of("sellerName", sellerName);
    }

    @Override
    public String code() {
        return ERROR_CODE.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return args;
    }
}
